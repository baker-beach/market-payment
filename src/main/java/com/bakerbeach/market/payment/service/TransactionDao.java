package com.bakerbeach.market.payment.service;

import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.QueryBuilder;

public class TransactionDao {
	
	private static final Logger log = LoggerFactory.getLogger(TransactionDao.class.getName());

	private MongoTemplate mongoTemplate;

	private String collectionName;
	
	public void saveOrUpdate(PaymentTransaction transaction) throws TransactionDaoException{
		try {
			transaction.setUpdatedAt(new Date());
			if(transaction.getCreatedAt() == null)
				transaction.setCreatedAt(new Date());
			QueryBuilder qb = QueryBuilder.start();
			qb.and(TransactionMongoConverter.KEY_ORDER_ID).is(transaction.getOrderId());
			getDBCollection().update(qb.get(),TransactionMongoConverter.encode(transaction),true,false);
		} catch (DuplicateKeyException e) {
			log.warn(ExceptionUtils.getMessage(e));
			throw new TransactionDaoException();
		} catch (Exception e) {
			log.error(ExceptionUtils.getMessage(e));
			throw new TransactionDaoException();
		}
	}
	
	public PaymentTransaction findByOrderId(String orderId) throws TransactionDaoException{
		try {
			QueryBuilder qb = QueryBuilder.start();
			qb.and(TransactionMongoConverter.KEY_ORDER_ID).is(orderId);
			DBObject dbo = getDBCollection().findOne(qb.get());
			if (dbo != null) {
				return TransactionMongoConverter.decode(dbo);
			} else {
				throw new TransactionDaoException();
			}
		} catch (Exception e) {
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
