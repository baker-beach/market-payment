package com.bakerbeach.market.payment.methods.paypal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

public abstract class AbstractPayPalMethod implements PaymentMethod {

	private String clientId;
	private String secret;
	private String token;
	private Date tokenExpires = new Date();
	private String mode;
	private String returnUrl;
	private String cancelUrl;
	private String paymentType = PaymentMethod.TYPE_PAYPAL;
	private APIContext apiContext; 
	private Map<String, String> configurationMap;

	private void init() {
		getLogger().debug("init configurationMap with mode: " + mode );
		configurationMap = new HashMap<String, String>();
		configurationMap.put("mode", mode);
	}

	protected void initOAuth2Token() {
		init();
		getLogger().debug("get OAuth token secret: " + secret + " clientid: " +  clientId);
		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(clientId, secret, configurationMap);
		try {
			token = tokenCredential.getAccessToken();
			Date createdAt = new Date();
			tokenExpires.setTime(createdAt.getTime() + (tokenCredential.expiresIn()-3600)*1000);
			getLogger().debug("token expires: " + tokenExpires);
		} catch (PayPalRESTException e) {
			getLogger().error(ExceptionUtils.getMessage(e));
			getLogger().debug(ExceptionUtils.getStackTrace(e));
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

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	protected abstract Logger getLogger();
	
}
