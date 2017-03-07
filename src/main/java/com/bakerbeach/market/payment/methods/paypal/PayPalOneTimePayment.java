package com.bakerbeach.market.payment.methods.paypal;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.CartItem;
import com.bakerbeach.market.core.api.model.CartItemQualifier;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.core.api.model.Total;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.api.service.PaymentServiceException.PaymentRedirectException;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.bakerbeach.market.payment.service.PaymentDataDao;
import com.bakerbeach.market.payment.service.TransactionDao;
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
import com.paypal.base.rest.PayPalRESTException;

public class PayPalOneTimePayment extends AbstractPayPalMethod {

	private static final Logger log = LoggerFactory.getLogger(PayPalOneTimePayment.class.getName());
	private static final String DEFAULT_METHOD_CODE = "PAYPAL_ONE_TIME";
	private boolean instantCapture = false;
	private String paymentMethodCode = DEFAULT_METHOD_CODE;
	private TransactionDao transactionDao;
	private PaymentDataDao paymentDataDao;
	private NumberFormat nf;

	public PayPalOneTimePayment() {
		nf = NumberFormat.getInstance(new Locale("en"));
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMinimumIntegerDigits(1);
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext) {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());
		try {
			PaymentData paymentData = paymentDataDao.findByCustomerId(paymentContext.getCustomerId());
			if (paymentData.getLastPaymemtMethodCode().equals(this.getPaymentMethodCode())) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.paypal");
				paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
				paymentContext.setPaymentValid(true);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter) {
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.paypal");
		paymentContext.setPaymentValid(true);
	}

	@Override
	public String getPaymentMethodCode() {
		return paymentMethodCode;
	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameter) throws PaymentServiceException {
		try {
			PaymentTransaction paymentTransaction = transactionDao.findByOrderId(paymentContext.getOrderId());

			if (parameter.get("paymentId").equals((String) paymentTransaction.getData().get("payment_id"))) {
				Map<String, Object> log = new HashMap<String, Object>();
				log.put("request", "payment_info");
				Payment payment = Payment.get(getApiContext(), parameter.get("paymentId"));
				log.put("response", payment.toJSON());
				String payerID = payment.getPayer().getPayerInfo().getPayerId();
				if (parameter.get("PayerID").equals(payerID)) {
					paymentTransaction.getData().put("payer_id", payerID);
					transactionDao.saveOrUpdate(paymentTransaction);
				}
			}
		} catch (Exception e) {
			throw new PaymentServiceException();
		}
	}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {

		try {
			PaymentTransaction paymentTransaction = transactionDao.findByOrderId(shopContext.getOrderId());
			if (!paymentTransaction.getData().containsKey("payer_id")) {
				throw new TransactionDaoException();
			}
		} catch (TransactionDaoException e) {
			doPayPalOrderCall(paymentContext, cart, shopContext);
		}

	}

	@Override
	public void doOrder(Order order) throws PaymentServiceException {

		try {
			Map<String, Object> log = new HashMap<String, Object>();
			log.put("request", "payment_execute");
			PaymentTransaction paymentTransaction = transactionDao.findByOrderId(order.getId());
			String paymentId = (String) paymentTransaction.getData().get("payment_id");
			Payment payment = Payment.get(getApiContext(), paymentId);
			PaymentExecution pe = new PaymentExecution();
			pe.setPayerId((String) paymentTransaction.getData().get("payer_id"));
			payment = payment.execute(getApiContext(), pe);
			log.put("response", payment.toJSON());
			paymentTransaction.getLog().add(log);
			transactionDao.saveOrUpdate(paymentTransaction);
			com.paypal.api.payments.Order paypalOrder = payment.getTransactions().get(0).getRelatedResources().get(0).getOrder();
			paymentTransaction.getData().put("paypal_order_id", paypalOrder.getId());
			Map<String, Object> logAuth = new HashMap<String, Object>();
			paypalOrder.getAmount().setDetails(null);
			log.put("request", "order_authorization");
			log.put("response", paypalOrder.authorize(getApiContext()).toJSON());
			paymentTransaction.getLog().add(logAuth);
			transactionDao.saveOrUpdate(paymentTransaction);
		} catch (TransactionDaoException e) {
			throw new PaymentServiceException();
		} catch (PayPalRESTException e) {
			throw new PaymentServiceException();
		}

		if (instantCapture) {
			doCapture(order, order.getTotal());
		}

		PaymentData paymentData;
		try {
			paymentData = paymentDataDao.findByCustomerId(order.getCustomerId());
		} catch (Exception e) {
			paymentData = new PaymentData();
			paymentData.setCustomerId(order.getCustomerId());
		}
		paymentData.setLastPaymemtMethodCode(this.getPaymentMethodCode());
		try {
			paymentDataDao.saveOrUpdate(paymentData);
		} catch (TransactionDaoException e) {
			e.printStackTrace();
		}
	}

	public void doPayPalOrderCall(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {

		PaymentTransaction paymentTransaction = new PaymentTransaction();

		paymentTransaction.setPaymentMethodCode(this.paymentMethodCode);
		paymentTransaction.setOrderId(shopContext.getOrderId());

		Transaction transaction = new Transaction();

		Amount amount = new Amount();
		amount.setTotal(nf.format(cart.getGrandTotal()));
		amount.setCurrency(paymentContext.getCurency());

		transaction.setAmount(amount);
		transaction.setDescription("This is the payment transaction description.");

		ItemList itemList = new ItemList();

		for (CartItem cartItem : cart.getCartItems()) {
			List<String> qualifiers = Arrays.asList(CartItemQualifier.PRODUCT, CartItemQualifier.VPRODUCT, CartItemQualifier.SERVICE, CartItemQualifier.SHIPPING);
			if (qualifiers.contains(cartItem.getQualifier())) {
				Item item = new Item();
				item.setName(cartItem.getTitle1() + " " + cartItem.getTitle2() + " " + cartItem.getTitle3());
				item.setPrice(nf.format(cartItem.getUnitPrice()));
				item.setCurrency(paymentContext.getCurency());
				item.setQuantity(((Integer) cartItem.getQuantity().intValue()).toString());
				itemList.getItems().add(item);
			}
		}

		Total discount = cart.getDiscount();
		if (discount != null && discount.getGross().compareTo(BigDecimal.ZERO) != 0) {
			Item item = new Item();
			item.setName("Discount");
			item.setPrice(nf.format(discount.getGross()));
			item.setCurrency(paymentContext.getCurency());
			item.setQuantity("1");
			itemList.getItems().add(item);
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

			log.put("request", payment.toJSON());

			createdPayment = payment.create(getApiContext());

			log.put("response", createdPayment.toJSON());

			paymentTransaction.getLog().add(log);

			paymentTransaction.getData().put("payment_id", createdPayment.getId());

			for (Links links : createdPayment.getLinks()) {
				if (links.getRel().equals("approval_url")) {
					String[] parts = links.getHref().split("token=");
					paymentTransaction.getData().put("token", parts[1]);
					throw new PaymentRedirectException(links.getHref());
				}
			}

		} catch (PayPalRESTException e) {
		} finally {
			try {
				transactionDao.saveOrUpdate(paymentTransaction);
			} catch (TransactionDaoException e) {
			}
		}
	}

	public void doCapture(Order order, BigDecimal amount) throws PaymentServiceException {

		try {
			PaymentTransaction paymentTransaction = transactionDao.findByOrderId(order.getId());
			String paypalOrderId = (String) paymentTransaction.getData().get("paypal_order_id");
			com.paypal.api.payments.Order paypalOrder = com.paypal.api.payments.Order.get(getApiContext(), paypalOrderId);
			Amount paypalAmount = new Amount();
			paypalAmount.setCurrency(order.getCurrency());
			paypalAmount.setTotal(nf.format(amount));
			Capture capture = new Capture();
			capture.setAmount(paypalAmount);
			capture.setIsFinalCapture(true);

			Map<String, Object> log = new HashMap<String, Object>();

			log.put("request", capture.toJSON());

			capture = paypalOrder.capture(getApiContext(), capture);

			log.put("response", capture.toJSON());

			paymentTransaction.getLog().add(log);

			transactionDao.saveOrUpdate(paymentTransaction);

			if (!(capture.getState().equals("completed") || capture.getState().equals("pending"))) {
				throw new PaymentServiceException(new MessageImpl(MessageImpl.TYPE_ERROR, "paypal.capture.error"));
			}

		} catch (TransactionDaoException e) {
			throw new PaymentServiceException();
		} catch (PayPalRESTException e) {
			throw new PaymentServiceException();
		}

	}

	protected Logger getLogger() {
		return log;
	}

	public TransactionDao getTransactionDao() {
		return transactionDao;
	}

	public void setTransactionDao(TransactionDao transactionDao) {
		this.transactionDao = transactionDao;
	}

	public PaymentDataDao getPaymentDataDao() {
		return paymentDataDao;
	}

	public void setPaymentDataDao(PaymentDataDao paymentDataDao) {
		this.paymentDataDao = paymentDataDao;
	}

	/**
	 * @return the instantCapture
	 */
	public boolean isInstantCapture() {
		return instantCapture;
	}

	/**
	 * @param instantCapture
	 *            the instantCapture to set
	 */
	public void setInstantCapture(boolean instantCapture) {
		this.instantCapture = instantCapture;
	}

	@Override
	public void doCancel(Order order) throws PaymentServiceException {
		// TODO Auto-generated method stub
		
	}

}