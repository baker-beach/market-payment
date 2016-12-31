package com.bakerbeach.market.payment.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentTransaction {
	
	private String orderId;
	private String paymentMethodCode;
	private Date updatedAt;
	private Date createdAt;
	private Map<String,Object> data = new HashMap<String,Object>();
	private List<Map<String,Object>> log = new ArrayList<Map<String,Object>>();
	
	/**
	 * @return the orderId
	 */
	public String getOrderId() {
		return orderId;
	}
	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	/**
	 * @return the paymentMethodId
	 */
	public String getPaymentMethodCode() {
		return paymentMethodCode;
	}
	/**
	 * @param paymentMethodId the paymentMethodId to set
	 */
	public void setPaymentMethodCode(String paymentMethodCode) {
		this.paymentMethodCode = paymentMethodCode;
	}
	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}
	/**
	 * @param updatedAt the updatedAt to set
	 */
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}
	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	/**
	 * @return the data
	 */
	public Map<String,Object> getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(Map<String,Object> data) {
		this.data = data;
	}
	/**
	 * @return the log
	 */
	public List<Map<String,Object>> getLog() {
		return log;
	}
	/**
	 * @param log the log to set
	 */
	public void setLog(List<Map<String,Object>> log) {
		this.log = log;
	}

}
