package com.bakerbeach.market.payment.service;

import java.util.HashMap;
import java.util.Map;

import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Customer;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.model.PaymentInfo;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.api.service.PaymentShopService;
import com.bakerbeach.market.payment.methods.PaymentShopMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentInfoImpl;

public class PaymentShopServiceImpl implements PaymentShopService {

	private Map<String, PaymentShopMethod> paymentMethods = new HashMap<String, PaymentShopMethod>();
	private PaymentContextManager paymentContextManager = new PaymentContextManager();
	private TransactionDao transactionDao;
	private PaymentDataDao paymentDataDao;

	@Override
	public PaymentInfo initPayment(ShopContext shopContext, Customer customer, Cart cart)
			throws PaymentServiceException {
		PaymentContext paymentContext = getPaymentContext(shopContext.getOrderId());
		paymentContext.setShopContext(shopContext);
		paymentContext.setCart(cart);
		paymentContext.setCustomerId(customer.getId());
		for (PaymentShopMethod paymentMethod : paymentMethods.values()) {
			paymentMethod.initCheckout(paymentContext, cart, shopContext);
		}
		return new PaymentInfoImpl(paymentContext);
	}

	@Override
	public PaymentInfo doPreOrder(Cart cart, ShopContext shopContext) throws PaymentServiceException {
		PaymentContext paymentContext = getPaymentContext(shopContext.getOrderId());
		PaymentShopMethod paymentMethod = paymentMethods
				.get(shopContext.getShopCode() + "|" + paymentContext.getCurrentPaymentMethodCode());
		paymentMethod.initOrder(paymentContext, cart, shopContext);
		return new PaymentInfoImpl(paymentContext);
	}
	
	@Override
	public PaymentInfo getPaymentInfo(ShopContext shopContext) {
		PaymentContext paymentContext = getPaymentContextManager().getPaymentContext(shopContext.getOrderId());
		return new PaymentInfoImpl(paymentContext);
	}
	
	@Override
	public PaymentInfo configPaymentMethod(ShopContext shopContext, Map<String, String> parameters)
			throws PaymentServiceException {
			PaymentContext paymentContext = getPaymentContextManager().getPaymentContext(shopContext.getOrderId());
			PaymentShopMethod paymentMethod = paymentMethods
					.get(shopContext.getShopCode() + "|" + shopContext.getRequestData().get("code"));
			if (paymentMethod != null) {
				paymentMethod.configPayment(paymentContext, parameters);
				return new PaymentInfoImpl(paymentContext);
			} else
				throw new PaymentServiceException();
	}
	
	@Override
	public PaymentInfo processReturn(ShopContext shopContext, Map<String, String> parameters)
			throws PaymentServiceException {
			PaymentContext paymentContext = getPaymentContextManager().getPaymentContext(shopContext.getOrderId());
			PaymentShopMethod paymentMethod = paymentMethods
					.get(shopContext.getShopCode() + "|" + shopContext.getRequestData().get("code"));
			if (paymentMethod != null) {
				paymentMethod.processReturn(paymentContext, parameters);
				return new PaymentInfoImpl(paymentContext);
			} else
				throw new PaymentServiceException();
	}
	
	private PaymentContext getPaymentContext(String id) {
		return paymentContextManager.getPaymentContext(id);
	}

	/**
	 * @return the paymentContextManager
	 */
	public PaymentContextManager getPaymentContextManager() {
		return paymentContextManager;
	}
	
	/**
	 * @param paymentContextManager
	 *            the paymentContextManager to set
	 */
	public void setPaymentContextManager(PaymentContextManager paymentContextManager) {
		this.paymentContextManager = paymentContextManager;
	}

	public Map<String, PaymentShopMethod> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(Map<String, PaymentShopMethod> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}
	
	/**
	 * @return the transactionDao
	 */
	public TransactionDao getTransactionDao() {
		return transactionDao;
	}

	/**
	 * @param transactionDao the transactionDao to set
	 */
	public void setTransactionDao(TransactionDao transactionDao) {
		this.transactionDao = transactionDao;
	}

	/**
	 * @return the paymentDataDao
	 */
	public PaymentDataDao getPaymentDataDao() {
		return paymentDataDao;
	}

	/**
	 * @param paymentDataDao the paymentDataDao to set
	 */
	public void setPaymentDataDao(PaymentDataDao paymentDataDao) {
		this.paymentDataDao = paymentDataDao;
	}

}
