package com.bakerbeach.market.payment.methods;

import java.util.Map;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.model.PaymentContext;

public interface PaymentShopMethod extends PaymentMethod{

	void initCheckout(PaymentContext paymentContext,Cart cart, ShopContext shopContext) throws PaymentServiceException;
	
	void initOrder(PaymentContext paymentContext,Cart cart, ShopContext shopContext) throws PaymentServiceException;
	
	void configPayment(PaymentContext paymentContext, Map<String,String> parameter) throws PaymentServiceException;
	
	void processReturn(PaymentContext paymentContext, Map<String, String> parameters) throws PaymentServiceException;
	
}
