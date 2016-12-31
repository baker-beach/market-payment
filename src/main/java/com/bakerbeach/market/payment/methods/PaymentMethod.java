package com.bakerbeach.market.payment.methods;

import java.math.BigDecimal;
import java.util.Map;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.model.PaymentContext;

public interface PaymentMethod {
	
	public static final String TYPE_INVOICE = "INVOICE";
	public static final String TYPE_NOTHING = "NOTHING";
	public static final String TYPE_PAYPAL = "PAYPAL";
	public static final String TYPE_CREDITCARD = "CREDITCARD";
	
	String getPaymentType();
	
	String getPaymentMethodCode();
	
	void initCheckout(PaymentContext paymentContext,Cart cart, ShopContext shopContext) throws PaymentServiceException;
	
	void initOrder(PaymentContext paymentContext,Cart cart, ShopContext shopContext) throws PaymentServiceException;
	
	void configPayment(PaymentContext paymentContext, Map<String,String> parameter) throws PaymentServiceException;
	
	void doOrder(Order order, PaymentContext paymentContext) throws PaymentServiceException;

	void processReturn(PaymentContext paymentContext, Map<String, String> parameters) throws PaymentServiceException;

	void doCapture(Order order, BigDecimal amount) throws PaymentServiceException;

}
