package util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HMAC {
    /**
     * Verwendet einen SHA512-Hashing-Algorithmus.
     */
    private static final String HMAC_SHA512 = "HmacSHA512";

    /**
     * Berechnet den HMAC einer gegeben Nachricht.
     * @param data Daten, die zum Erstellen eines HMC notwendig sind.
     * @param key Der Key fuer den HMAC-Algorithmus.
     * @return Der HMAC-Value
     * @throws NoSuchAlgorithmException Algorithmus nicht vorhanden
     * @throws InvalidKeyException Key nicht vorhanden
     */
    public static String calculateHMAC(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA512);
        Mac mac = Mac.getInstance(HMAC_SHA512);
        mac.init(secretKeySpec);
        byte[] bytes = mac.doFinal(data.getBytes());
        StringBuilder sb = new StringBuilder(bytes.length*2);
        for(byte aByte : bytes) {
            sb.append(String.format("%02x", aByte));
        }
        return sb.toString();
    }
}
