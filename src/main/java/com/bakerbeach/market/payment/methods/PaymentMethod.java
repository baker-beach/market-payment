package com.bakerbeach.market.payment.methods;

public interface PaymentMethod {
	
	public static final String TYPE_INVOICE = "INVOICE";
	public static final String TYPE_NOTHING = "NOTHING";
	public static final String TYPE_PAYPAL = "PAYPAL";
	public static final String TYPE_CREDITCARD = "CREDITCARD";
	
	String getPaymentType();
	
	String getPaymentMethodCode();
}
