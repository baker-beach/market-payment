package com.bakerbeach.market.payment.methods;

import java.math.BigDecimal;
import java.util.Map;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.service.PaymentDataDao;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public abstract class AbstractPaymentMethod implements PaymentServiceMethod,PaymentShopMethod{

	private PaymentDataDao paymentDataDao;	
		
	public PaymentDataDao getPaymentDataDao() {
		return paymentDataDao;
	}

	public void setPaymentDataDao(PaymentDataDao paymentDataDao) {
		this.paymentDataDao = paymentDataDao;
	}
	
	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {		
	}

	@Override
	public void doOrder(Order order) throws PaymentServiceException {
		PaymentData paymentData;
		try{
			paymentData = paymentDataDao.findByCustomerId(order.getCustomerId());
		}catch(Exception e){
			paymentData = new PaymentData();
			paymentData.setCustomerId(order.getCustomerId());
		}
		paymentData.setLastPaymemtMethodCode(this.getPaymentMethodCode());
		try {
			paymentDataDao.saveOrUpdate(paymentData);
		} catch (TransactionDaoException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameter)
			throws PaymentServiceException {
	}

	@Override
	public void doCapture(Order order, BigDecimal amount) {
	}

	@Override
	public void doCancel(Order order) throws PaymentServiceException {
	}
	
}
