package com.bakerbeach.market.payment.methods.concardis;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.order.api.model.Order;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.AbstractPaymentMethod;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public abstract class AbstractFlexCheckout extends AbstractPaymentMethod implements PaymentMethod {
	
	private final String URL_TOKENPAGE = "https://payengine.test.v-psp.com/Tokenization/Hostedpage";
	private final String URL_TOKENPAGE_PROD = "https://secure.payengine.de/Tokenization/HostedPage";
	private final String URL_ORDER = "https://secure.payengine.de/ncol/[mode]/orderdirect.asp";
	private final String URL_MAINTENANCE = "https://secure.payengine.de/ncol/[mode]/maintenancedirect.asp";
	private final String URL_ECOMMERCE = "https://secure.payengine.de/ncol/[mode]/orderstandard_utf8.asp";

	private String brand;
	private String paymentMethod;
	private String returnUrl;
	private String cancelUrl;
	private String secret;
	private String pspId;
	private String mode = "test";

	protected boolean instantCapture = false;

	protected String getiFrameUrl(String aliasId, String appPath, String lang) {

		MultiValueMap<String, String> urlParameter = new LinkedMultiValueMap<String, String>();
		urlParameter.add("ALIAS.ALIASID", aliasId);
		urlParameter.add("ACCOUNT.PSPID", getPspId());
		urlParameter.add("ALIAS.STOREPERMANENTLY", "Y");
		urlParameter.add("LAYOUT.LANGUAGE", lang);
		urlParameter.add("PARAMETERS.ACCEPTURL", appPath + getReturnUrl());
		urlParameter.add("PARAMETERS.EXCEPTIONURL", appPath + getCancelUrl());
		urlParameter.add("CARD.PAYMENTMETHOD", paymentMethod);
		if (brand != null)
			urlParameter.add("CARD.BRAND", brand);

		urlParameter.add("SHASIGNATURE.SHASIGN", sha1(urlParameter));

		StringBuilder sb = new StringBuilder(getTokenpageUrl()).append("?");

		Set<String> keys = urlParameter.keySet();
		List<String> sortedKeys = new ArrayList<String>(keys);
		Collections.sort(sortedKeys);

		for (String key : sortedKeys) {
			try {
				sb.append(key).append("=").append(URLEncoder.encode(urlParameter.get(key).get(0), "UTF-8"));
				if (!key.equals("SHASIGNATURE.SHASIGN"))
					sb.append("&");
			} catch (UnsupportedEncodingException e) {
			}
		}

		return sb.toString();
	}

	private String sha1(MultiValueMap<String, String> parameter) {
		try {
			StringBuilder src = new StringBuilder();
			Set<String> keys = parameter.keySet();
			List<String> sortedKeys = new ArrayList<String>(keys);
			Collections.sort(sortedKeys);
			for (String key : sortedKeys) {
				src.append(key).append("=").append(parameter.get(key).get(0)).append(secret);
			}

			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(src.toString().getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return "";
		}

	}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {
	}

	protected final String getTokenpageUrl() {
		if (mode.equals("test"))
			return URL_TOKENPAGE;
		else
			return URL_TOKENPAGE_PROD;
	}

	@Override
	public void doOrder(Order order, PaymentContext paymentContext) throws PaymentServiceException {
		doReservation(order);
		if (instantCapture)
			doCapture(order, order.getTotal(true).getGross());
	}

	private void doReservation(Order order) throws PaymentServiceException {

		PaymentData pd = getPaymentData(order.getCustomerId());

		@SuppressWarnings("rawtypes")
		Map paymentData = (Map) pd.getPaymentData().get(getPaymentMethodCode());

		if (!paymentData.containsKey("AliasId"))
			throw new PaymentServiceException();

		BigDecimal amount = order.getTotal(true).getGross().multiply(new BigDecimal(100));

		MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

		parameter.add("PSPID", getPspId());
		parameter.add("ORDERID", order.getId());
		parameter.add("USERID", getUserId());
		parameter.add("PSWD", getPassword());
		parameter.add("AMOUNT", (new Integer(amount.intValue())).toString());
		parameter.add("CURRENCY", order.getCurrencyCode());
		parameter.add("OPERATION", "RES");
		parameter.add("PM", getPaymentMethod());
		parameter.add("ALIAS", (String) paymentData.get("AliasId"));
		parameter.add("ECI", "9");
		parameter.add("SHASIGN", sha1(parameter));

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
				getLogger().error("error concardis reservation order:" + order.getId());
				throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.reservation.error"));
			}
		} catch (TransactionDaoException | DocumentException e) {
			getLogger().error("error concardis reservation order:" + order.getId());
			throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.reservation.error"));
		}
	}

	@Override
	public void doCapture(Order order, BigDecimal amount) {
		try {
			PaymentTransaction paymentTransaction = getPaymentTransactionData(order.getId());

			MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

			parameter.add("PSPID", getPspId());
			parameter.add("ORDERID", order.getId());
			parameter.add("USERID", getUserId());
			parameter.add("PSWD", getPassword());
			parameter.add("AMOUNT", (new Integer(amount.multiply(new BigDecimal(100)).intValue())).toString());
			parameter.add("OPERATION", "SAL");
			parameter.add("PAYID", (String) paymentTransaction.getData().get("PAYID"));
			parameter.add("SHASIGN", sha1(parameter));

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
		} catch (Exception e) {
			getLogger().error("error concardis capture order:" + order.getId());
		}

	}

	private String password;
	private String userId;

	private RestTemplate restTemplate = new RestTemplate();

	protected final String getOrderUrl() {
		return URL_ORDER.replace("[mode]", mode);
	}

	protected final String getMaintenanceUrl() {
		return URL_MAINTENANCE.replace("[mode]", mode);
	}

	protected final String getECommerceUrl() {
		return URL_ECOMMERCE.replace("[mode]", mode);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
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

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getPspId() {
		return pspId;
	}

	public void setPspId(String pspId) {
		this.pspId = pspId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	protected abstract Logger getLogger();

}
