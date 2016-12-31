package com.bakerbeach.market.payment.service;

import com.bakerbeach.market.payment.model.PaymentContext;

public class PaymentContextManager{
	
	PaymentContextStore paymentContextStore = new PaymentContextStore();

	public PaymentContext getPaymentContext(String id) {
		PaymentContext paymentContext = paymentContextStore.get(id);
		if(paymentContext == null){
			paymentContext = new PaymentContext();
			paymentContextStore.put(id, paymentContext);
		}
		return paymentContext;
	}

}
