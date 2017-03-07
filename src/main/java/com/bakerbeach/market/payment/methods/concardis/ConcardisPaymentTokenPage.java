package com.bakerbeach.market.payment.methods.concardis;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.bakerbeach.market.core.api.model.Cart;
import com.bakerbeach.market.core.api.model.ShopContext;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.PaymentShopMethod;
import com.bakerbeach.market.payment.model.PaymentContext;
import com.bakerbeach.market.payment.model.PaymentData;
import com.bakerbeach.market.payment.service.TransactionDaoException;

public class ConcardisPaymentTokenPage extends AbstractConcardisPayment implements PaymentShopMethod {

	@Override
	public String getPaymentType() {
		return TYPE_CREDITCARD;
	}

	@Override
	public String getPaymentMethodCode() {
		return "CONCARDIS_CREDITCARD";
	}

	@Override
	public void initCheckout(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {
		paymentContext.getPaymentDataMap().put(getPaymentMethodCode(), new HashMap<String, Object>());

		MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

		parameter.add("ACCOUNT.PSPID", getPspId());
		parameter.add("ALIAS.ALIASID", paymentContext.getCustomerId());
		parameter.add("CARD.PAYMENTMETHOD", "CreditCard");
		parameter.add("PARAMETERS.ACCEPTURL", "http://dev.demo.com/demo/concardis/success/");
		parameter.add("PARAMETERS.EXCEPTIONURL", "http://dev.demo.com/demo/concardis/cancel/");
		parameter.add("SHASIGNATURE.SHASIGN", ConcardisSignatureHelper.sha1(parameter,getSecret()));

		StringBuilder sb = new StringBuilder("https://payengine.test.v-psp.com/Tokenization/Hostedpage?");

		for (String key : parameter.keySet()) {
			try {
				sb.append(key).append("=").append(URLEncoder.encode(parameter.get(key).get(0), "UTF-8"));
				if (!key.equals("SHASIGNATURE.SHASIGN"))
					sb.append("&");
			} catch (UnsupportedEncodingException e) {
			}
		}
		paymentContext.getPaymentDataMap().get(getPaymentMethodCode()).put("iframe_href", sb.toString());
	}

	@Override
	public void initOrder(PaymentContext paymentContext, Cart cart, ShopContext shopContext)
			throws PaymentServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processReturn(PaymentContext paymentContext, Map<String, String> parameters)
			throws PaymentServiceException {

		PaymentData pd = new PaymentData();
		try {
			pd = getPaymentDataDao().findByCustomerId(paymentContext.getCustomerId());
		} catch (TransactionDaoException e) {
			pd.setCustomerId(paymentContext.getCustomerId());
		}

		Map<String, String> params = new HashMap<String, String>();

		params.put("Card.ExpiryDate", (String) parameters.get("Card.ExpiryDate"));
		params.put("Card.CardHolderName", (String) parameters.get("Card.CardHolderName"));
		params.put("Card.CardNumber", (String) parameters.get("Card.CardNumber"));
		params.put("Card.Brand", (String) parameters.get("Card.Brand"));
		params.put("Alias.AliasId", (String) parameters.get("Alias.AliasId"));
		pd.getPaymentData().put(getPaymentMethodCode(), params);
		try {
			getPaymentDataDao().saveOrUpdate(pd);
			paymentContext.setCurrentPaymentMethodCode(getPaymentMethodCode());
			paymentContext.setPaymentValid(true);
		} catch (Exception e) {
			throw new PaymentServiceException();
		}

	}

	@Override
	public void configPayment(PaymentContext paymentContext, Map<String, String> parameter)
			throws PaymentServiceException {

	}

}
