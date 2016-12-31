package com.bakerbeach.market.payment.methods.concardis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.util.MultiValueMap;

public class ConcardisSignatureHelper {
	
	public static String sha1(MultiValueMap<String, String> parameter, String secret) {
		try {
			StringBuilder src = new StringBuilder();
			Set<String> keys = parameter.keySet();
			List<String> sortedKeys = new ArrayList<String>(keys);
			Collections.sort(sortedKeys);
			for (String key : sortedKeys) {
				src.append(key).append("=").append(parameter.get(key).get(0)).append(secret);
			}

			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(src.toString().getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return "";
		}

	}

}
