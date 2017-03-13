package com.bakerbeach.market.payment.model;

import java.util.HashMap;
import java.util.Map;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;

public class PaymentContext {
	
	private Map<String, Map<String, Object>> paymentDataMap = new HashMap<String, Map<String, Object>>();
	
	private boolean paymentValid = false;
	
	private String currentPaymentMethodCode = "";
	
	private Cart cart;
	
	private String customerId;
	
	private ShopContext shopContext;
	
	public Map<String, Map<String, Object>> getPaymentDataMap() {
		return paymentDataMap;
	}

	public String getCurrentPaymentMethodCode() {
		return currentPaymentMethodCode;
	}
	
	public void setCurrentPaymentMethodCode(String currentPaymentMethodCode) {
		this.currentPaymentMethodCode = currentPaymentMethodCode;
	}

	public Boolean isPaymentValid() {
		return paymentValid;
	}
	
	public void setPaymentValid(boolean paymentValid) {
		this.paymentValid = paymentValid;
	}
	
	public Cart getCart(){
		return cart;
	}
	
	public String getCurency(){
		return shopContext.getCurrentCurrency().getIsoCode();
	}

	/**
	 * @param cart the cart to set
	 */
	public void setCart(Cart cart) {
		this.cart = cart;
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
	 * @return the orderId
	 */
	public String getOrderId() {
		return shopContext.getOrderId();
	}

	/**
	 * @return the shopContext
	 */
	public ShopContext getShopContext() {
		return shopContext;
	}

	/**
	 * @param shopContext the shopContext to set
	 */
	public void setShopContext(ShopContext shopContext) {
		this.shopContext = shopContext;
	}


}
