package com.bakerbeach.market.payment.methods.paypal;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.CartItem;
import com.bakerbeach.market.core.api.model.CartItemQualifier;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.order.api.model.Order;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.api.service.PaymentServiceException.PaymentRedirectException;
import com.bakerbeach.market.payment.methods.AbstractPaymentMethod;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.bakerbeach.market.payment.service.TransactionDaoException;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalOneTimePayment extends AbstractPaymentMethod {

	private static final Logger Logger = LoggerFactory.getLogger(PayPalOneTimePayment.class.getName());

	private boolean instantCapture = false;
	private String clientId;
	private String secret;
	private String token;
	private Date tokenExpires = new Date();
	private String mode;
	private String returnUrl;
	private String cancelUrl;
	private APIContext apiContext;
	private Map<String, String> configurationMap;

	private NumberFormat nf;
	{
		nf = NumberFormat.getInstance(new Locale("en"));
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMinimumIntegerDigits(1);
	}

	@Override
	public String getPaymentMethodCode() {
		return "PAYPAL_ONE_TIME";
	}

	@Override
	public String getPaymentType() {
		return PaymentMethod.TYPE_PAYPAL;
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext) {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());

		PaymentData paymentData;
		try {
			paymentData = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
		} catch (TransactionDaoException e) {
			paymentData = new PaymentData();
		}

		if (paymentContext.getCurrentPaymentMethodCode().equals("")) {
			if (paymentData.getLastPaymemtMethodCode() != null && paymentData.getLastPaymemtMethodCode().equals(this.getPaymentMethodCode())) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.paypal");
				paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
				paymentContext.setPaymentValid(true);
			}
		} else if (paymentContext.getCurrentPaymentMethodCode().equals(getPaymentMethodCode())) {
			paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.paypal");
		}

	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter) {
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.paypal");
		paymentContext.setPaymentValid(true);
	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameter) throws PaymentServiceException {

		if (parameter.containsKey("result") && !parameter.get("result").equals("cancel")) {

			try {
				PaymentTransaction paymentTransaction = getTransactionDao().findByOrderId(paymentContext.getOrderId());

				if (parameter.get("paymentId").equals((String) paymentTransaction.getData().get("payment_id"))) {
					Map<String, Object> log = new HashMap<String, Object>();
					log.put("request", "payment_info");
					Payment payment = Payment.get(getApiContext(), parameter.get("paymentId"));
					log.put("response", payment.toJSON());
					String payerID = payment.getPayer().getPayerInfo().getPayerId();
					if (parameter.get("PayerID").equals(payerID)) {
						paymentTransaction.getData().put("payer_id", payerID);
						getTransactionDao().saveOrUpdate(paymentTransaction);
					}
				}
			} catch (Exception e) {
				throw new PaymentServiceException(new MessageImpl("paypal.return.errror", Message.TYPE_ERROR, "paypal.return.errror", Arrays.asList("box"), null));
			}
		} else
			throw new PaymentServiceException(new MessageImpl("paypal.return.cancel", Message.TYPE_ERROR, "paypal.return.cancel", Arrays.asList("box"), null));
	}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {

		try {
			PaymentTransaction paymentTransaction = getTransactionDao().findByOrderId(shopContext.getOrderId());
			if (!paymentTransaction.getData().containsKey("payer_id")) {
				throw new TransactionDaoException();
			}
		} catch (TransactionDaoException e) {
			doPayPalOrderCall(paymentContext, cart, shopContext);
		}

	}

	@Override
	public void doOrder(Order order, PaymentContext paymentContext) throws PaymentServiceException {

		doOrder(order);

		if (instantCapture) {
			doCapture(order, order.getTotal(true).getGross());
		}

		PaymentData paymentData;
		try {
			paymentData = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
		} catch (Exception e) {
			paymentData = new PaymentData();
			paymentData.setCustomerId(paymentContext.getCustomerId());
		}
		paymentData.setLastPaymemtMethodCode(this.getPaymentMethodCode());
		try {
			getPaymentDataDao().saveOrUpdate(paymentData);
		} catch (TransactionDaoException e) {
			Logger.error("error saving paymentData order:" + order.getId());
		}
	}

	private void doOrder(Order order) throws PaymentServiceException {
		try {
			Map<String, Object> log = new HashMap<String, Object>();
			log.put("request", "payment_execute");
			PaymentTransaction paymentTransaction = getTransactionDao().findByOrderId(order.getId());
			String paymentId = (String) paymentTransaction.getData().get("payment_id");
			Payment payment = Payment.get(getApiContext(), paymentId);
			PaymentExecution pe = new PaymentExecution();
			pe.setPayerId((String) paymentTransaction.getData().get("payer_id"));
			payment = payment.execute(getApiContext(), pe);
			log.put("response", payment.toJSON());
			paymentTransaction.getLog().add(log);
			getTransactionDao().saveOrUpdate(paymentTransaction);
			com.paypal.api.payments.Order paypalOrder = payment.getTransactions().get(0).getRelatedResources().get(0).getOrder();
			paymentTransaction.getData().put("paypal_order_id", paypalOrder.getId());
			Map<String, Object> logAuth = new HashMap<String, Object>();
			paypalOrder.getAmount().setDetails(null);
			log.put("request", "order_authorization");
			log.put("response", paypalOrder.authorize(getApiContext()).toJSON());
			paymentTransaction.getLog().add(logAuth);
			getTransactionDao().saveOrUpdate(paymentTransaction);
		} catch (TransactionDaoException e) {
			Logger.error("error saving paymentTransaction order:" + order.getId());
			throw new PaymentServiceException(new MessageImpl("paypal.order.error", Message.TYPE_ERROR, "paypal.order.error", Arrays.asList("box"), null));
		} catch (PayPalRESTException e) {
			Logger.error("error paypal order:" + order.getId());
			throw new PaymentServiceException(new MessageImpl("paypal.order.error", Message.TYPE_ERROR, "paypal.order.error", Arrays.asList("box"), null));
		}
	}

	public void doPayPalOrderCall(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {

		PaymentTransaction paymentTransaction = new PaymentTransaction();

		paymentTransaction.setPaymentMethodCode(getPaymentMethodCode());
		paymentTransaction.setOrderId(shopContext.getOrderId());

		Transaction transaction = new Transaction();

		Amount amount = new Amount();
		amount.setTotal(nf.format(cart.getGrandTotal()));
		amount.setCurrency(paymentContext.getCurency());

		transaction.setAmount(amount);
		transaction.setDescription("This is the payment transaction description.");

		ItemList itemList = new ItemList();

		List<String> qualifiers = Arrays.asList(CartItemQualifier.PRODUCT, CartItemQualifier.VPRODUCT);
		for (CartItem cartItem : cart.getItems().values()) {
			if (qualifiers.contains(cartItem.getQualifier())) {
				Item item = new Item();
				item.setName(cartItem.getTitle("title1") + " " + cartItem.getTitle("title2") + " " + cartItem.getTitle("title3"));
				item.setPrice(nf.format(cartItem.getUnitPrice("std")));
				item.setCurrency(paymentContext.getCurency());
				item.setQuantity(((Integer) cartItem.getQuantity().intValue()).toString());
				itemList.getItems().add(item);
			}
		}
		qualifiers = Arrays.asList(CartItemQualifier.SERVICE, CartItemQualifier.SHIPPING, CartItemQualifier.DISCOUNT);
		for (CartItem cartItem : cart.getItems().values()) {
			if (qualifiers.contains(cartItem.getQualifier()) && (cartItem.getTotalPrice("std") != BigDecimal.ZERO)) {
				Item item = new Item();
				item.setName(cartItem.getTitle("title1") + " " + cartItem.getTitle("title2") + " " + cartItem.getTitle("title3"));
				item.setPrice(nf.format(cartItem.getUnitPrice("std")));
				item.setCurrency(paymentContext.getCurency());
				item.setQuantity(((Integer) cartItem.getQuantity().intValue()).toString());
				itemList.getItems().add(item);
			}
		}

		// ##TODO
		// ShippingAddress shippingAddress = new ShippingAddress();
		//
		// shippingAddress.setCity(shopContext.getShippingAddress().getCity());
		// shippingAddress.setCountryCode(shopContext.getShippingAddress().getCountryCode());
		// shippingAddress.setPostalCode(shopContext.getShippingAddress().getPostcode());
		//
		// String name = "";
		// if(shopContext.getShippingAddress().getFirstName() != null)
		// name = name + shopContext.getShippingAddress().getFirstName();
		// if(shopContext.getShippingAddress().getMiddleName() != null)
		// name = name + " " + shopContext.getShippingAddress().getMiddleName();
		// if(shopContext.getShippingAddress().getLastName() != null)
		// name = name + " " + shopContext.getShippingAddress().getLastName();
		//
		// shippingAddress.setRecipientName(name);
		//
		// if(shopContext.getShippingAddress().getRegion() != null)
		// shippingAddress.setState(shopContext.getShippingAddress().getRegion());
		//
		// shippingAddress.setLine1(shopContext.getShippingAddress().getStreet1());
		//
		// if(shopContext.getShippingAddress().getStreet2() != null)
		// shippingAddress.setLine2(shopContext.getShippingAddress().getStreet2());
		//
		// itemList.setShippingAddress(shippingAddress);

		transaction.setItemList(itemList);

		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");

		Payment payment = new Payment();
		payment.setIntent("order");
		payment.setPayer(payer);
		payment.setTransactions(transactions);

		RedirectUrls redirectUrls = new RedirectUrls();

		String applicationPath = shopContext.getApplicationPath();

		redirectUrls.setCancelUrl(applicationPath + getCancelUrl());
		redirectUrls.setReturnUrl(applicationPath + getReturnUrl());

		payment.setRedirectUrls(redirectUrls);

		Payment createdPayment = null;
		try {

			Map<String, Object> log = new HashMap<String, Object>();

			paymentTransaction.getLog().add(log);

			log.put("request", payment.toJSON());

			createdPayment = payment.create(getApiContext());

			log.put("response", createdPayment.toJSON());

			paymentTransaction.getData().put("payment_id", createdPayment.getId());

			for (Links links : createdPayment.getLinks()) {
				if (links.getRel().equals("approval_url")) {
					String[] parts = links.getHref().split("token=");
					paymentTransaction.getData().put("token", parts[1]);
					throw new PaymentRedirectException(links.getHref());
				}
			}

		} catch (PaymentRedirectException e) {
			throw e;
		} catch (Exception e) {
			throw new PaymentServiceException(new MessageImpl("paypal.init.error", Message.TYPE_ERROR, "paypal.init.error", Arrays.asList("box"), null));
		} finally {
			try {
				getTransactionDao().saveOrUpdate(paymentTransaction);
			} catch (TransactionDaoException e) {
			}
		}
	}

	public void doCapture(Order order, BigDecimal amount) throws PaymentServiceException {
		try {
			PaymentTransaction paymentTransaction = getTransactionDao().findByOrderId(order.getId());
			String paypalOrderId = (String) paymentTransaction.getData().get("paypal_order_id");
			com.paypal.api.payments.Order paypalOrder = com.paypal.api.payments.Order.get(getApiContext(), paypalOrderId);
			Amount paypalAmount = new Amount();
			paypalAmount.setCurrency(order.getCurrencyCode());
			paypalAmount.setTotal(nf.format(amount));
			Capture capture = new Capture();
			capture.setAmount(paypalAmount);
			capture.setIsFinalCapture(true);

			Map<String, Object> log = new HashMap<String, Object>();

			log.put("request", capture.toJSON());

			capture = paypalOrder.capture(getApiContext(), capture);

			log.put("response", capture.toJSON());

			paymentTransaction.getLog().add(log);

			getTransactionDao().saveOrUpdate(paymentTransaction);

			if (!(capture.getState().equals("completed") || capture.getState().equals("pending"))) {
				throw new PaymentServiceException(new MessageImpl("paypal.capture.error", Message.TYPE_ERROR, "paypal.capture.error", Arrays.asList("box"), Arrays.asList()));
			}

		} catch (TransactionDaoException | PayPalRESTException e) {
			throw new PaymentServiceException(new MessageImpl("paypal.capture.error", Message.TYPE_ERROR, "paypal.capture.error", Arrays.asList("box"), Arrays.asList()));
		}

	}

	public boolean isInstantCapture() {
		return instantCapture;
	}

	public void setInstantCapture(boolean instantCapture) {
		this.instantCapture = instantCapture;
	}

	private void init() {
		Logger.debug("init configurationMap with mode: " + mode);
		configurationMap = new HashMap<String, String>();
		configurationMap.put("mode", mode);
	}

	protected void initOAuth2Token() {
		init();
		Logger.debug("get OAuth token secret: " + secret + " clientid: " + clientId);
		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(clientId, secret, configurationMap);
		try {
			token = tokenCredential.getAccessToken();
			Date createdAt = new Date();
			tokenExpires.setTime(createdAt.getTime() + (tokenCredential.expiresIn() - 3600) * 1000);
			Logger.debug("token expires: " + tokenExpires);
		} catch (PayPalRESTException e) {
			Logger.error(ExceptionUtils.getMessage(e));
			Logger.debug(ExceptionUtils.getStackTrace(e));
		}
	}

	public String getToken() {
		if (token != null) {
			if (tokenExpires.before(new Date()))
				initOAuth2Token();
		} else {
			initOAuth2Token();
		}
		return token;
	}

	public APIContext getApiContext() {
		apiContext = new APIContext(getToken());
		apiContext.setConfigurationMap(configurationMap);
		return apiContext;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getCancelUrl() {
		return cancelUrl;
	}

	public void setCancelUrl(String cancelUrl) {
		this.cancelUrl = cancelUrl;
	}

}