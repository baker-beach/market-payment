package com.bakerbeach.market.payment.service;

import java.util.HashMap;

import com.bakerbeach.market.payment.model.PaymentContext;

public class PaymentContextStore extends HashMap<String, PaymentContext>{

	private static final long serialVersionUID = 1L;
	
	public PaymentContext get(String id){
		return super.get(id);
	}

}
