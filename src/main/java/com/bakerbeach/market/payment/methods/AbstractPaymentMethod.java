package com.bakerbeach.market.payment.methods;

import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.bakerbeach.market.payment.service.PaymentDataDao;
import com.bakerbeach.market.payment.service.TransactionDao;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public abstract class AbstractPaymentMethod implements PaymentMethod {

	private TransactionDao transactionDao;
	private PaymentDataDao paymentDataDao;

	protected PaymentTransaction getPaymentTransactionData(String orderId) {

		try {
			return getTransactionDao().findByOrderId(orderId);
		} catch (TransactionDaoException e) {
			PaymentTransaction paymentTransaction = new PaymentTransaction();
			paymentTransaction.setOrderId(orderId);
			paymentTransaction.setPaymentMethodCode(getPaymentMethodCode());
			return paymentTransaction;
		}

	}

	protected PaymentData getPaymentData(String customerId) {

		try {
			return getPaymentDataDao().findByCustomerId(customerId);
		} catch (TransactionDaoException e) {
			PaymentData paymentData = new PaymentData();
			paymentData.setCustomerId(customerId);
			return paymentData;
		}

	}

	public TransactionDao getTransactionDao() {
		return transactionDao;
	}

	public void setTransactionDao(TransactionDao transactionDao) {
		this.transactionDao = transactionDao;
	}

	public PaymentDataDao getPaymentDataDao() {
		return paymentDataDao;
	}

	public void setPaymentDataDao(PaymentDataDao paymentDataDao) {
		this.paymentDataDao = paymentDataDao;
	}

}
