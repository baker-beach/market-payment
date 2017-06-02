package com.bakerbeach.market.payment.methods.concardis;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.NotImplementedException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.order.api.model.Order;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public class ConcardisPaymentTokenPage extends AbstractConcardisPayment implements PaymentMethod {

	private boolean instantCapture = true;

	@Override
	public String getPaymentType() {
		return TYPE_CREDITCARD;
	}

	@Override
	public String getPaymentMethodCode() {
		return "CONCARDIS_CREDITCARD";
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());

		MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

		parameter.add("ACCOUNT.PSPID", getPspId());
		parameter.add("ALIAS.ALIASID", paymentContext.getCustomerId());
		parameter.add("CARD.PAYMENTMETHOD", "CreditCard");
		parameter.add("ALIAS.STOREPERMANENTLY", "Y");
		parameter.add("LAYOUT.LANGUAGE", shopContext.getCurrentLocale().toString());
		parameter.add("PARAMETERS.ACCEPTURL", shopContext.getApplicationPath() + getReturnUrl());
		parameter.add("PARAMETERS.EXCEPTIONURL", shopContext.getApplicationPath() + getCancelUrl());
		parameter.add("SHASIGNATURE.SHASIGN", ConcardisSignatureHelper.sha1(parameter, getSecret()));

		StringBuilder sb = new StringBuilder(getTokenpageUrl()).append("?");

		for (String key : parameter.keySet()) {
			try {
				sb.append(key).append("=").append(URLEncoder.encode(parameter.get(key).get(0), "UTF-8"));
				if (!key.equals("SHASIGNATURE.SHASIGN"))
					sb.append("&");
			} catch (UnsupportedEncodingException e) {
			}
		}
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("iframe_href", sb.toString());

		try {
			PaymentData pd = getPaymentData(paymentContext.getCustomerId());
			Map params = (Map) pd.getPaymentData().get(getPaymentMethodCode());
			if (params.containsKey("Brand")) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("ExpiryDate", params.get("ExpiryDate"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardHolderName", params.get("CardHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardNumber", params.get("CardNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("Brand", params.get("Brand"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("card", 1);
			
				if(paymentContext.getCurrentPaymentMethodCode().equals("")){
					if (pd.getLastPaymemtMethodCode() != null && pd.getLastPaymemtMethodCode().equals(getPaymentMethodCode())) {
						paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");
						paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
						paymentContext.setPaymentValid(true);
					}
				}else if(paymentContext.getCurrentPaymentMethodCode().equals(getPaymentMethodCode())){
					paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {
	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameters) throws PaymentServiceException {

		if (parameters.get("Alias.Status").equals("0") || parameters.get("Alias.Status").equals("2")) {

			PaymentData pd = new PaymentData();
			try {
				pd = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
			} catch (TransactionDaoException e) {
				pd.setCustomerId(paymentContext.getCustomerId());
			}

			Map<String, String> params = new HashMap<String, String>();

			params.put("ExpiryDate", (String) parameters.get("Card.ExpiryDate"));
			params.put("CardHolderName", (String) parameters.get("Card.CardHolderName"));
			params.put("CardNumber", (String) parameters.get("Card.CardNumber"));
			params.put("Brand", (String) parameters.get("Card.Brand"));
			params.put("AliasId", (String) parameters.get("Alias.AliasId"));
			pd.getPaymentData().put(getPaymentMethodCode(), params);
			try {
				getPaymentDataDao().saveOrUpdate(pd);
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("ExpiryDate", params.get("ExpiryDate"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardHolderName", params.get("CardHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardNumber", params.get("CardNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("Brand", params.get("Brand"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("card", 1);
				paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
				paymentContext.setPaymentValid(true);
			} catch (Exception e) {
				throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.return.error"));
			}
		} else {
			throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.return.error"));
		}
	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter) throws PaymentServiceException {
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");

		try {
			PaymentData pd = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
			Map params = (Map) pd.getPaymentData().get(getPaymentMethodCode());
			if (params.containsKey("Brand")) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("ExpiryDate", params.get("ExpiryDate"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardHolderName", params.get("CardHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardNumber", params.get("CardNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("Brand", params.get("Brand"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("card", 1);
			}
		} catch (Exception e) {
		}

		paymentContext.setPaymentValid(true);
	}

	@Override
	public void doOrder(Order order, PaymentContext paymentContext) throws PaymentServiceException {
		doReservation(order);
		if (instantCapture)
			doCapture(order, order.getTotal());
	}

	private void doReservation(Order order) throws PaymentServiceException {

		PaymentData pd = getPaymentData(order.getCustomerId());

		@SuppressWarnings("rawtypes")
		Map paymentData = (Map) pd.getPaymentData().get(getPaymentMethodCode());

		if (!paymentData.containsKey("AliasId"))
			throw new PaymentServiceException();

		BigDecimal amount = order.getTotal().multiply(new BigDecimal(100));

		MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

		parameter.add("PSPID", getPspId());
		parameter.add("ORDERID", order.getId());
		parameter.add("USERID", getUserId());
		parameter.add("PSWD", getPassword());
		parameter.add("AMOUNT", (new Integer(amount.intValue())).toString());
		parameter.add("CURRENCY", order.getCurrency());
		parameter.add("OPERATION", "RES");
		parameter.add("PM", "CreditCard");
		parameter.add("ALIAS", (String) paymentData.get("AliasId"));
		parameter.add("ECI", "9");
		parameter.add("SHASIGN", ConcardisSignatureHelper.sha1(parameter, getSecret()));

		@SuppressWarnings("serial")
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(parameter, new HttpHeaders() {
			{
				setContentType(new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8")));
			}
		});
		String result = getRestTemplate().postForObject(getOrderUrl(), entity, String.class);
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new StringReader(result));
			Element ncresponse = document.getRootElement();

			PaymentTransaction paymentTransaction = getPaymentTransactionData(order.getId());

			Map<String, Object> log = new HashMap<String, Object>();
			log.put("request", "reservation");
			log.put("response", result);

			paymentTransaction.getLog().add(log);

			if (ncresponse.attribute("STATUS").getStringValue().equals("5")) {
				paymentTransaction.getData().put("PAYID", ncresponse.attribute("PAYID").getStringValue());
				getTransactionDao().saveOrUpdate(paymentTransaction);
				pd.setLastPaymemtMethodCode(getPaymentMethodCode());
				getPaymentDataDao().saveOrUpdate(pd);

			} else {
				getTransactionDao().saveOrUpdate(paymentTransaction);
				throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.reservation.error"));
			}
		} catch (TransactionDaoException | DocumentException e) {
			throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.reservation.error"));
		}
	}

	@Override
	public void doCapture(Order order, BigDecimal amount) {

		PaymentTransaction paymentTransaction = getPaymentTransactionData(order.getId());

		MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

		parameter.add("PSPID", getPspId());
		parameter.add("ORDERID", order.getId());
		parameter.add("USERID", getUserId());
		parameter.add("PSWD", getPassword());
		parameter.add("AMOUNT", (new Integer(amount.multiply(new BigDecimal(100)).intValue())).toString());
		parameter.add("OPERATION", "SAL");
		parameter.add("PAYID", (String) paymentTransaction.getData().get("PAYID"));
		parameter.add("SHASIGN", ConcardisSignatureHelper.sha1(parameter, getSecret()));

		@SuppressWarnings("serial")
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(parameter, new HttpHeaders() {
			{
				setContentType(new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8")));
			}
		});
		String result = getRestTemplate().postForObject(getMaintenanceUrl(), entity, String.class);

		Map<String, Object> log = new HashMap<String, Object>();
		log.put("request", "capture");
		log.put("response", result);
		paymentTransaction.getLog().add(log);
		try {
			getTransactionDao().saveOrUpdate(paymentTransaction);
		} catch (TransactionDaoException e) {
		}

	}

	protected PaymentTransaction getPaymentTransactionData(String orderId) {

		try {
			return getTransactionDao().findByOrderId(orderId);
		} catch (TransactionDaoException e) {
			PaymentTransaction paymentTransaction = new PaymentTransaction();
			paymentTransaction.setOrderId(orderId);
			paymentTransaction.setPaymentMethodCode(getPaymentMethodCode());
			return paymentTransaction;
		}

	}

	protected PaymentData getPaymentData(String customerId) {

		try {
			return getPaymentDataDao().findByCustomerId(customerId);
		} catch (TransactionDaoException e) {
			PaymentData paymentData = new PaymentData();
			paymentData.setCustomerId(customerId);
			return paymentData;
		}

	}

	public boolean isInstantCapture() {
		return instantCapture;
	}

	public void setInstantCapture(boolean instantCapture) {
		this.instantCapture = instantCapture;
	}

}
