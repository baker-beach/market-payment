package com.bakerbeach.market.payment.service;

import java.util.Date;

import com.bakerbeach.market.payment.model.PaymentData;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class PaymentDataMongoConverter {
	
	public static final String KEY_CUSTOMER_ID = "customer_id";
	public static final String KEY_PAYMENT_DATA = "payment_data";
	public static final String KEY_CREATED_AT = "created_at";
	public static final String KEY_UPDATED_AT = "updated_at";
	public static final String KEY_LAST_PAYMENT_METHOD_CODE = "last_payment_method_code";
	
	@SuppressWarnings("unchecked")
	public static PaymentData decode(DBObject dbo){
		PaymentData paymentData = new PaymentData();
		paymentData.setCustomerId((String)dbo.get(KEY_CUSTOMER_ID));
		paymentData.setCreatedAt((Date)dbo.get(KEY_CREATED_AT));
		paymentData.setUpdatedAt((Date)dbo.get(KEY_UPDATED_AT));
		paymentData.setPaymentData(((DBObject)dbo.get(KEY_PAYMENT_DATA)).toMap());
		paymentData.setLastPaymemtMethodCode((String)dbo.get(KEY_LAST_PAYMENT_METHOD_CODE));
		return paymentData;
	}
	
	public static DBObject encode(PaymentData paymentData){
		DBObject dbo = new BasicDBObject();
		dbo.put(KEY_CUSTOMER_ID, paymentData.getCustomerId());
		dbo.put(KEY_PAYMENT_DATA, paymentData.getPaymentData());
		dbo.put(KEY_CREATED_AT, paymentData.getCreatedAt());
		dbo.put(KEY_UPDATED_AT, paymentData.getUpdatedAt());
		dbo.put(KEY_LAST_PAYMENT_METHOD_CODE, paymentData.getLastPaymemtMethodCode());
		return dbo;
	}

}
