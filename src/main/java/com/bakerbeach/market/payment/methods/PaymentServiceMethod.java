package com.bakerbeach.market.payment.methods;

import java.math.BigDecimal;

import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;

public interface PaymentServiceMethod extends PaymentMethod{

	void doCapture(Order order, BigDecimal amount) throws PaymentServiceException;
	
	void doOrder(Order order) throws PaymentServiceException;
	
	void doCancel(Order order) throws PaymentServiceException;
	
}
