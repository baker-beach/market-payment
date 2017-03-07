package com.bakerbeach.market.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.payment.api.service.PaymentService;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.PaymentServiceMethod;

public class PaymentServiceImpl implements PaymentService {

	private Map<String, PaymentServiceMethod> paymentMethods = new HashMap<String, PaymentServiceMethod>();

	@Override
	public void doOrder(Order order) throws PaymentServiceException {
		PaymentServiceMethod paymentMethod = paymentMethods
				.get(order.getShopCode() + "|" + order.getPaymentCode());
		if (paymentMethod != null) {
			order.setPaymentCode(paymentMethod.getPaymentMethodCode());
			paymentMethod.doOrder(order);
		} else
			throw new PaymentServiceException(new MessageImpl(MessageImpl.TYPE_ERROR,"payment.error"));

	}

	@Override
	public void doCancel(Order order) throws PaymentServiceException {

	}

	@Override
	public void doCapture(Order order, BigDecimal amount) throws PaymentServiceException {

	}

	public Map<String, PaymentServiceMethod> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(Map<String, PaymentServiceMethod> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}
}
