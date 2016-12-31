package com.bakerbeach.market.payment.methods.concardis;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.commons.lang.NotImplementedException;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.bakerbeach.market.payment.model.PaymentContext;

public class ConcardisPaymentDirectLinkMethod extends AbstractConcardisPayment implements PaymentMethod {

	@Override
	public String getPaymentType() {
		return TYPE_CREDITCARD;
	}

	@Override
	public String getPaymentMethodCode() {
		return "CONCARDIS_CREDITCARD";
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {
		throw new NotImplementedException();
	}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameters)
			throws PaymentServiceException {
		throw new NotImplementedException();
	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter)
			throws PaymentServiceException {

	}

	@Override
	public void doOrder(Order order, PaymentContext paymentContext) throws PaymentServiceException {
		throw new NotImplementedException();
	}

	@Override
	public void doCapture(Order order, BigDecimal amount) {
		// TODO Auto-generated method stub
		
	}

}
