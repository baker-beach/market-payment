package com.bakerbeach.market.payment.methods.concardis;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.api.service.PaymentServiceException.PaymentRedirectException;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public class ConcardisPaymentECommerceMethod extends AbstractConcardisPayment implements PaymentMethod {

	@Override
	public String getPaymentType() {
		return TYPE_CREDITCARD;
	}

	@Override
	public String getPaymentMethodCode() {
		return "CONCARDIS_CREDITCARD_ECOMMERCE";
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());
	}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {
		try {
			PaymentTransaction paymentTransaction = getTransactionDao().findByOrderId(shopContext.getOrderId());
			if(!paymentTransaction.getData().containsKey("result")){
				throw new PaymentServiceException();
			}
			if(!paymentTransaction.getData().get("result").equals("accept")){
				throw new PaymentServiceException();
			}
		} catch (TransactionDaoException|PaymentServiceException e) {
			PaymentTransaction paymentTransaction = new PaymentTransaction();
			paymentTransaction.setPaymentMethodCode(getPaymentMethodCode());
			paymentTransaction.setOrderId(shopContext.getOrderId());
			try {
				getTransactionDao().saveOrUpdate(paymentTransaction);
			} catch (TransactionDaoException e2) {
			}
			paymentTransaction.setOrderId(shopContext.getOrderId());
			
			MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

			parameter.add("PSPID", getPspId());
			parameter.add("ORDERID", shopContext.getOrderId());
			parameter.add("AMOUNT", (new Integer(cart.getGrandTotal().multiply(new BigDecimal(100)).intValue()).toString()));
			parameter.add("CURRENCY", shopContext.getCurrency());
			parameter.add("LANGUAGE", shopContext.getCurrentLocale().toLanguageTag());
			
			parameter.add("ACCEPTURL", shopContext.getApplicationPath()+"/concardis/accept/");
			parameter.add("DECLINEURL", shopContext.getApplicationPath()+"/concardis/decline/");
			parameter.add("EXCEPTIONURL", shopContext.getApplicationPath()+"/concardis/exception/");
			parameter.add("CANCELURL", shopContext.getApplicationPath()+"/concardis/cancel/");
			
			
			
			parameter.add("SHASIGN", sha1(parameter));

			StringBuilder url = new StringBuilder(getUrl());

			for (String key : parameter.keySet()) {
				try {
					url.append(key).append("=").append(URLEncoder.encode(parameter.get(key).get(0), "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
				}
				if (!key.equals("SHASIGN"))
					url.append("&");

			}

			throw new PaymentRedirectException(url.toString());
		}

	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameters)
			throws PaymentServiceException {
		try {
			PaymentTransaction paymentTransaction = getTransactionDao().findByOrderId(paymentContext.getOrderId());
			if(paymentTransaction != null){
				if(parameters.containsKey("result")){
					MultiValueMap<String, String> param = new LinkedMultiValueMap<String, String>();
					for(String key : parameters.keySet()){
						if(!key.equals("result") && !key.equals("SHASIGN")){
							param.add(key, parameters.get(key));
						}
					}
//					String hash = sha1(param);
//					if(!hash.equals(parameters.get("SHASIGN"))){
//						throw new PaymentServiceException();
//					}
					paymentTransaction.getData().putAll(parameters);
					getTransactionDao().saveOrUpdate(paymentTransaction);
	
					if(!parameters.get("result").equals("accept")){
						throw new PaymentServiceException();
					}
				}else
					throw new PaymentServiceException();
			}
			PaymentData paymentData;
			try {
				paymentData = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
			} catch (Exception e) {
				paymentData = new PaymentData();
				paymentData.setCustomerId(paymentContext.getCustomerId());
			}
			paymentData.setLastPaymemtMethodCode(this.getPaymentMethodCode());
			try {
				getPaymentDataDao().saveOrUpdate(paymentData);
			} catch (TransactionDaoException e) {
				e.printStackTrace();
			}
		
		} catch (TransactionDaoException e) {
			throw new PaymentServiceException();
		}
		


	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter)
			throws PaymentServiceException {
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text",
				"payment.dashboard.text.concardis_ecommerce");
		paymentContext.setPaymentValid(true);

	}

	@Override
	public void doOrder(Order order, PaymentContext paymentContext) throws PaymentServiceException {

	}


	@Override
	public void doCapture(Order order, BigDecimal amount) {
		throw new NotImplementedException();		
	}

}
