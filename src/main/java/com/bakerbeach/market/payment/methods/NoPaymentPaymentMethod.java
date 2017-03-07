package com.bakerbeach.market.payment.methods;

import java.util.HashMap;
import java.util.Map;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;

public class NoPaymentPaymentMethod extends AbstractPaymentMethod {

	@Override
	public String getPaymentType() {
		// TODO Auto-generated method stub
		return PaymentMethod.TYPE_NOTHING;
	}

	@Override
	public String getPaymentMethodCode() {
		// TODO Auto-generated method stub
		return PaymentMethod.TYPE_NOTHING;
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext) {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());
		try {
			PaymentData paymentData = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
			if (paymentData.getLastPaymemtMethodCode().equals(this.getPaymentMethodCode())) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text",
						"payment.dashboard.text.nothing");
				paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
				paymentContext.setPaymentValid(true);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter) {
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.nothing");
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("args", "");
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.setPaymentValid(true);
	}

}
