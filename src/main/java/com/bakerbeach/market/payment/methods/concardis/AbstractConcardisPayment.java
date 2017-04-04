package com.bakerbeach.market.payment.methods.concardis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.bakerbeach.market.payment.service.PaymentDataDao;
import com.bakerbeach.market.payment.service.TransactionDao;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public abstract class AbstractConcardisPayment {
	
	private final String URL_TOKENPAGE = "https://payengine.test.v-psp.com/Tokenization/Hostedpage";
	private final String URL_TOKENPAGE_PROD = "https://secure.payengine.de/Tokenization/HostedPage";
	private final String URL_ORDER = "https://secure.payengine.de/ncol/[mode]/orderdirect.asp";
	private final String URL_MAINTENANCE = "https://secure.payengine.de/ncol/[mode]/maintenancedirect.asp";
	private final String URL_ECOMMERCE = "https://secure.payengine.de/ncol/[mode]/orderstandard_utf8.asp";
	
	private String mode = "test";
	private String secret;
	private String password;
	private String userId;
	private String pspId;
	private RestTemplate restTemplate = new RestTemplate();
	private String returnUrl;
	private String cancelUrl;


	private TransactionDao transactionDao;
	private PaymentDataDao paymentDataDao;

	public String sha1(MultiValueMap<String, String> parameter) {
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
	
	protected final String getOrderUrl(){
		return URL_ORDER.replace("[mode]", mode);
	}
	
	protected final String getMaintenanceUrl(){
		return URL_MAINTENANCE.replace("[mode]", mode);
	}
	
	protected final String getTokenpageUrl(){
		if(mode.equals("test"))
			return URL_TOKENPAGE;
		else
			return URL_TOKENPAGE_PROD;
	}
	
	protected final String getECommerceUrl(){
		return URL_TOKENPAGE.replace("[mode]", mode);
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
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

	public String getPspId() {
		return pspId;
	}

	public void setPspId(String pspId) {
		this.pspId = pspId;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
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

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
