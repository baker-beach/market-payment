package com.bakerbeach.market.payment.methods.concardis;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public class ConcardisCreditCardFlexCheckout extends AbstractFlexCheckout implements PaymentMethod {

	private static final Logger Logger = LoggerFactory.getLogger(ConcardisCreditCardFlexCheckout.class.getName());


	@Override
	public String getPaymentType() {
		return TYPE_CREDITCARD;
	}

	@Override
	public String getPaymentMethodCode() {
		return "CONCARDIS_CREDITCARD";
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("iframe_href", getiFrameUrl(paymentContext.getCustomerId(), shopContext.getApplicationPath(), shopContext.getCurrentLocale().toString()));

		try {
			PaymentData pd = getPaymentData(paymentContext.getCustomerId());
			@SuppressWarnings("unchecked")
			Map<String, Object> params = (Map<String, Object>) pd.getPaymentData().get(getPaymentMethodCode());
			if (params.containsKey("Brand")) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("ExpiryDate", params.get("ExpiryDate"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardHolderName", params.get("CardHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardNumber", params.get("CardNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("Brand", params.get("Brand"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("card", 1);

				if (paymentContext.getCurrentPaymentMethodCode().equals("")) {
					if (pd.getLastPaymemtMethodCode() != null && pd.getLastPaymemtMethodCode().equals(getPaymentMethodCode())) {
						paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");
						paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
						paymentContext.setPaymentValid(true);
					}
				} else if (paymentContext.getCurrentPaymentMethodCode().equals(getPaymentMethodCode())) {
					paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameters) throws PaymentServiceException {

		if (parameters.get("Alias.Status").equals("0") || parameters.get("Alias.Status").equals("2")) {

			PaymentData pd = new PaymentData();
			try {
				pd = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
			} catch (TransactionDaoException e) {
				pd.setCustomerId(paymentContext.getCustomerId());
			}

			Map<String, String> params = new HashMap<String, String>();

			params.put("ExpiryDate", (String) parameters.get("Card.ExpiryDate"));
			params.put("CardHolderName", (String) parameters.get("Card.CardHolderName"));
			params.put("CardNumber", (String) parameters.get("Card.CardNumber"));
			params.put("Brand", (String) parameters.get("Card.Brand"));
			params.put("AliasId", (String) parameters.get("Alias.AliasId"));
			pd.getPaymentData().put(getPaymentMethodCode(), params);
			try {
				getPaymentDataDao().saveOrUpdate(pd);
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("ExpiryDate", params.get("ExpiryDate"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardHolderName", params.get("CardHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardNumber", params.get("CardNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("Brand", params.get("Brand"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("card", 1);
				paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
				paymentContext.setPaymentValid(true);
			} catch (Exception e) {
				throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.return.error"));
			}
		} else {
			throw new PaymentServiceException(new MessageImpl(Message.TYPE_ERROR, "concardis.return.error"));
		}
	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter) throws PaymentServiceException {
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis");

		try {
			PaymentData pd = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
			@SuppressWarnings("unchecked")
			Map<String, Object> params = (Map<String, Object>) pd.getPaymentData().get(getPaymentMethodCode());
			if (params.containsKey("Brand")) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("ExpiryDate", params.get("ExpiryDate"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardHolderName", params.get("CardHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("CardNumber", params.get("CardNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("Brand", params.get("Brand"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("card", 1);
			}
		} catch (Exception e) {
		}

		paymentContext.setPaymentValid(true);
	}

	@Override
	protected Logger getLogger() {
		return Logger;
	}
}
