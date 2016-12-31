package com.bakerbeach.market.payment.methods.concardis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bakerbeach.market.payment.service.PaymentDataDao;
import com.bakerbeach.market.payment.service.TransactionDao;

public abstract class AbstractConcardisPayment {

	private String url;
	private String secret;
	private String password;
	private String userId;
	private String pspId;
	private RestTemplate restTemplate;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

}
