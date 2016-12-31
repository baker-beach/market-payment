package com.bakerbeach.market.payment.service;

import java.util.Date;

import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class TransactionMongoConverter {
	
	public static final String KEY_ORDER_ID = "order_id";
	public static final String KEY_PAYMENT_METHOD = "payment_method_code";
	public static final String KEY_TRANSACTION_LOG = "transaction_log";
	public static final String KEY_TRANSACTION_DATA = "transaction_data";
	public static final String KEY_CREATED_AT = "created_at";
	public static final String KEY_UPDATED_AT = "updated_at";
	
	@SuppressWarnings("unchecked")
	public static PaymentTransaction decode(DBObject dbo){
		PaymentTransaction transaction = new PaymentTransaction();
		transaction.setOrderId((String)dbo.get(KEY_ORDER_ID));
		transaction.setPaymentMethodCode((String)dbo.get(KEY_PAYMENT_METHOD));
		transaction.setCreatedAt((Date)dbo.get(KEY_CREATED_AT));
		transaction.setUpdatedAt((Date)dbo.get(KEY_UPDATED_AT));
		transaction.setData(((DBObject)dbo.get(KEY_TRANSACTION_DATA)).toMap());
		
		for (Object item : ((BasicDBList) dbo.get(KEY_TRANSACTION_LOG))) {
			transaction.getLog().add(((DBObject)item).toMap());
		}
		
		return transaction;
	}
	
	public static DBObject encode(PaymentTransaction transaction){
		DBObject dbo = new BasicDBObject();
		dbo.put(KEY_ORDER_ID, transaction.getOrderId());
		dbo.put(KEY_PAYMENT_METHOD, transaction.getPaymentMethodCode());
		dbo.put(KEY_TRANSACTION_LOG, transaction.getLog());
		dbo.put(KEY_TRANSACTION_DATA, transaction.getData());
		dbo.put(KEY_CREATED_AT, transaction.getCreatedAt());
		dbo.put(KEY_UPDATED_AT, transaction.getUpdatedAt());
		return dbo;
	}

}
