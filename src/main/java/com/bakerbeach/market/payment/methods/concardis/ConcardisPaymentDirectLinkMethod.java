package com.bakerbeach.market.payment.methods.concardis;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.bakerbeach.market.core.api.model.Order;
import com.bakerbeach.market.payment.api.service.PaymentServiceException;
import com.bakerbeach.market.payment.methods.PaymentServiceMethod;

public class ConcardisPaymentDirectLinkMethod extends AbstractConcardisPayment implements PaymentServiceMethod {

	@Override
	public String getPaymentType() {
		return TYPE_CREDITCARD;
	}

	@Override
	public String getPaymentMethodCode() {
		return "CONCARDIS_CREDITCARD";
	}


	@Override
	public void doCapture(Order order, BigDecimal amount) {
	}

	@Override
	public void doCancel(Order order) throws PaymentServiceException {
	}
	
	@Override
	public void doOrder(Order order) throws PaymentServiceException {
		doReservation(order, order.getCustomerId());
	}

	private void doReservation(Order order, String aliasId) {

		BigDecimal amount = order.getTotal().multiply(new BigDecimal(100));

		MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();

		parameter.add("PSPID", getPspId());
		parameter.add("ORDERID", order.getId());
		parameter.add("USERID", getUserId());
		parameter.add("PSWD", getPassword());
		parameter.add("AMOUNT", (new Integer(amount.intValue())).toString());
		parameter.add("CURRENCY", order.getCurrency());
		parameter.add("OPERATION", "RES");
		parameter.add("ALIAS.ALIASID", aliasId);
		parameter.add("SHASIGNATURE.SHASIGN", ConcardisSignatureHelper.sha1(parameter,getSecret()));

		@SuppressWarnings("serial")
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(parameter,
				new HttpHeaders() {
					{
						setContentType(new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8")));
					}
				});
		String result = getRestTemplate().postForObject(getUrl(), entity, String.class);
		System.out.println(result);
	}

}
