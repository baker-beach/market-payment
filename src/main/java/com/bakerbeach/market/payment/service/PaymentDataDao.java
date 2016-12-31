package com.bakerbeach.market.payment.service;

import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.bakerbeach.market.payment.model.PaymentData;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.QueryBuilder;

public class PaymentDataDao {
	
	private static final Logger log = LoggerFactory.getLogger(PaymentDataDao.class.getName());

	private MongoTemplate mongoTemplate;

	private String collectionName;

	public void saveOrUpdate(PaymentData paymentData) throws TransactionDaoException{
		try {
			if(paymentData.getCreatedAt() == null)
				paymentData.setCreatedAt(new Date());
			paymentData.setUpdatedAt(new Date());
			QueryBuilder qb = QueryBuilder.start();
			qb.and(PaymentDataMongoConverter.KEY_CUSTOMER_ID).is(paymentData.getCustomerId());
			getDBCollection().update(qb.get(),PaymentDataMongoConverter.encode(paymentData), true, false);
		} catch (DuplicateKeyException e) {
			log.warn(ExceptionUtils.getMessage(e));
			throw new TransactionDaoException();
		} catch (Exception e) {
			log.error(ExceptionUtils.getMessage(e));
			throw new TransactionDaoException();
		}
	}
	
	public PaymentData findByCustomerId(String customerId) throws TransactionDaoException{
		try {
			QueryBuilder qb = QueryBuilder.start();
			qb.and(PaymentDataMongoConverter.KEY_CUSTOMER_ID).is(customerId);
			DBObject dbo = getDBCollection().findOne(qb.get());
			if (dbo != null) {
				return PaymentDataMongoConverter.decode(dbo);
			} else {
				throw new TransactionDaoException();
			}
		} catch (Exception e) {
			log.error(ExceptionUtils.getMessage(e));
			throw new TransactionDaoException();
		}
	}
	
	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public void setMongoTemplate(MongoTemplate mongoShopTemplate) {
		this.mongoTemplate = mongoShopTemplate;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	private DBCollection getDBCollection() {
		return mongoTemplate.getCollection(collectionName);
	}

}
