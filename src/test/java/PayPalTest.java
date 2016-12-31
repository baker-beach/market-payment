import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.bakerbeach.market.payment.model.PaymentTransaction;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.Order;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalTest {

	public static void main(String[] args) {
		String clientId="AR6qonaailh1S42YOjXa5SF8FhVseEawm2c5gKp_HhPEa_u7gnArcLtkmZNO8m7DBE6Q8bzkf4bDTf18";
		String secret="EGvHCmnC8SWaXNZ5zgqiaMnhGmqOSJoFpzv0BrZgiGL2DqwLHzcFjb23I4KfU4yEfDKOB9k3l5jfI5Ib";
		String token="";
		String mode="sandbox";
		APIContext apiContext;
		
		Map<String, String> configurationMap = configurationMap = new HashMap<String, String>();
		configurationMap.put("mode", mode);
		
		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(clientId, secret, configurationMap);
		try {
			token = tokenCredential.getAccessToken();
			apiContext = new APIContext(token);
			apiContext.setConfigurationMap(configurationMap);
			Payment payment = Payment.get(apiContext, "PAY-5B4241620V831503WLALQGNA");
			System.out.println(payment);
//			PaymentExecution pe = new PaymentExecution();
//			pe.setPayerId("YWERUXFMGDFWS");
//			payment = payment.execute(apiContext, pe);
//			Order order = payment.getTransactions().get(0).getRelatedResources().get(0).getOrder();
//			Order order = Order.get(apiContext, "O-8NC328724M1750307");
//			order.getAmount().setDetails(null);
			//Order order = new Order();
			//order.setId("O-8NC328724M1750307");
//			System.out.println(order);
//			order.authorize(apiContext);
//			System.out.println(order);
			
//			order.setId("O-46835482GU686943T");
//			Amount amount = new Amount();
//			amount.setCurrency("EUR");
//			amount.setTotal("9.90");
////			order.setAmount(amount);
//			Capture capture = new Capture();
//			capture.setAmount(amount);
//			capture.setIsFinalCapture(true);
//			capture = order.capture(apiContext, capture);
			Capture capture = Capture.get(apiContext, "9C167823F4399333Y");
//			capture.
			System.out.println(capture);
//			System.out.println(order);
		//System.out.println(payment);
		} catch (PayPalRESTException e) {
		}

	}

}
