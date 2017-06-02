package com.bakerbeach.market.payment.methods;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.order.api.model.Order;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.service.PaymentDataDao;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public class NoPaymentPaymentMethod implements PaymentMethod{
	
	private PaymentDataDao paymentDataDao;

	public PaymentDataDao getPaymentDataDao() {
		return paymentDataDao;
	}

	public void setPaymentDataDao(PaymentDataDao paymentDataDao) {
		this.paymentDataDao = paymentDataDao;
	}

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
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter)
			throws PaymentServiceException {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String,Object>());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.nothing");
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("args", "");
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.setPaymentValid(true);
		
	}

	@Override
	public void doOrder(Order order, PaymentContext paymentContext) throws PaymentServiceException {
		PaymentData paymentData;
		try{
			paymentData = paymentDataDao.findByCustomerId(paymentContext.getCustomerId());
		}catch(Exception e){
			paymentData = new PaymentData();
			paymentData.setCustomerId(paymentContext.getCustomerId());
		}
		paymentData.setLastPaymemtMethodCode(this.getPaymentMethodCode());
		try {
			paymentDataDao.saveOrUpdate(paymentData);
		} catch (TransactionDaoException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameters)
			throws PaymentServiceException {}

	@Override
	public void doCapture(Order order, BigDecimal amount) {}

}
