package com.bakerbeach.market.payment.model;

import java.util.Map;

import com.bakerbeach.market.payment.api.model.PaymentInfo;

public class PaymentInfoImpl implements PaymentInfo {
	
	private String paymentContextId; 
	
	private Boolean paymentValid = false;
	
	private Map<String, Map<String, Object>> paymentDataMap;
	
	private String currentPaymentMethodCode;
	
	public PaymentInfoImpl(PaymentContext paymentContext){
		paymentDataMap = paymentContext.getPaymentDataMap();
		paymentValid = paymentContext.isPaymentValid();
		currentPaymentMethodCode = paymentContext.getCurrentPaymentMethodCode();
	}

	@Override
	public Map<String, Map<String, Object>> getPaymentDataMap() {
		return paymentDataMap;
	}

	@Override
	public String getCurrentPaymentMethodCode() {
		return currentPaymentMethodCode;
	}

	@Override
	public Boolean isPaymentValid() {
		return paymentValid;
	}

	/**
	 * @return the paymentContextId
	 */
	public String getPaymentContextId() {
		return paymentContextId;
	}

	/**
	 * @param paymentContextId the paymentContextId to set
	 */
	public void setPaymentContextId(String paymentContextId) {
		this.paymentContextId = paymentContextId;
	}

	/**
	 * @param paymentValid the paymentValid to set
	 */
	public void setPaymentValid(Boolean paymentValid) {
		this.paymentValid = paymentValid;
	}

	/**
	 * @param currentPaymentMethodId the currentPaymentMethodId to set
	 */
	public void setCurrentPaymentMethodCode(String currentPaymentMethodCode) {
		this.currentPaymentMethodCode = currentPaymentMethodCode;
	}

}
