package com.bakerbeach.market.payment.methods.concardis;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bakerbeach.market.commons.Message;
import com.bakerbeach.market.commons.MessageImpl;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.PaymentMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public class ConcardisSepaFlexCheckout extends AbstractFlexCheckout implements PaymentMethod {

	private static final Logger Logger = LoggerFactory.getLogger(ConcardisSepaFlexCheckout.class.getName());

	@Override
	public String getPaymentType() {
		return TYPE_DIRECT_DEBIT;
	}

	@Override
	public String getPaymentMethodCode() {
		return "CONCARDIS_DIRECT_DEBIT";
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext) throws PaymentServiceException {

		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("iframe_href", getiFrameUrl(paymentContext.getCustomerId(), shopContext.getApplicationPath(), shopContext.getCurrentLocale().toString()));

		try {
			PaymentData pd = getPaymentData(paymentContext.getCustomerId());
			@SuppressWarnings("unchecked")
			Map<String, Object> params = (Map<String, Object>) pd.getPaymentData().get(getPaymentMethodCode());
			if (params.containsKey("AccountNumber")) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("accountHolderName", params.get("AccountHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("accountNumber", params.get("AccountNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("account", 1);

				if (paymentContext.getCurrentPaymentMethodCode().equals("")) {
					if (pd.getLastPaymemtMethodCode() != null && pd.getLastPaymemtMethodCode().equals(getPaymentMethodCode())) {
						paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis.DirectDebit");
						paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
						paymentContext.setPaymentValid(true);
					}
				} else if (paymentContext.getCurrentPaymentMethodCode().equals(getPaymentMethodCode())) {
					paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis.DirectDebit");
				}
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}

	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter) throws PaymentServiceException {
		paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis.DirectDebit");

		try {
			PaymentData pd = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
			@SuppressWarnings("unchecked")
			Map<String, Object> params = (Map<String, Object>) pd.getPaymentData().get(getPaymentMethodCode());
			if (params.containsKey("Brand")) {
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("accountHolderName", params.get("AccountHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("accountNumber", params.get("AccountNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("account", 1);
			}
		} catch (Exception e) {
		}

		paymentContext.setPaymentValid(true);
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

			params.put("AccountHolderName", (String) parameters.get("Card.CardHolderName"));
			params.put("AccountNumber", (String) parameters.get("Card.CardNumber"));
			params.put("AliasId", (String) parameters.get("Alias.AliasId"));
			pd.getPaymentData().put(getPaymentMethodCode(), params);
			try {
				getPaymentDataDao().saveOrUpdate(pd);
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("text", "payment.dashboard.text.concardis.DirectDebit");
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("accountHolderName", params.get("AccountHolderName"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("accountNumber", params.get("AccountNumber"));
				paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("account", 1);
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
	protected Logger getLogger() {
		return Logger;
	}

}
