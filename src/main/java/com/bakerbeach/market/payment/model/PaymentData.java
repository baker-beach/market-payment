package com.bakerbeach.market.payment.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentData{
	
	private String customerId;
	private Date updatedAt;
	private Date createdAt;
	private String lastPaymemtMethodCode;
	private Map<String,Object> paymentData = new HashMap<String,Object>();
	
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	/**
	 * @return the customerId
	 */
	public String getCustomerId() {
		return customerId;
	}
	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	/**
	 * @return the paymentData
	 */
	public Map<String,Object> getPaymentData() {
		return paymentData;
	}
	/**
	 * @param paymentData the paymentData to set
	 */
	public void setPaymentData(Map<String,Object> paymentData) {
		this.paymentData = paymentData;
	}
	/**
	 * @return the lastPaymemtMethodCode
	 */
	public String getLastPaymemtMethodCode() {
		return lastPaymemtMethodCode;
	}
	/**
	 * @param lastPaymemtMethodCode the lastPaymemtMethodCode to set
	 */
	public void setLastPaymemtMethodCode(String lastPaymemtMethodCode) {
		this.lastPaymemtMethodCode = lastPaymemtMethodCode;
	}


}
