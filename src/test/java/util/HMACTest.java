package util;

import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HMACTest {
    private String correctHash = "bba8ae88b98404c5b2346d68e814bb2fd591d9c588c5b6909dbd16daf" +
            "c68f36dbedb609905c1301edde5bb73de9e1ebfebec42341a1edc4b458f1cf1355c19b5";
    @Test
    public void calculateHMAC_correct() throws NoSuchAlgorithmException, InvalidKeyException {
        String actual = HMAC.calculateHMAC("testPassword", "ThisIsAHashKey");
        assertEquals(correctHash, actual);
    }

    @Test
    public void calculateHMAC_notcorrect() throws NoSuchAlgorithmException, InvalidKeyException {
        String actual = HMAC.calculateHMAC("testPassword", "ThisIsAFalseHashKey");
        assertFalse(actual.contentEquals(correctHash));
    }
}
