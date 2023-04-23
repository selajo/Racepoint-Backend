package api.util.admin;

import com.google.gson.annotations.Expose;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Ein Token wird fuer die Authentifizierung fuer ein Admin-Panel-Request via REST-API verwendet.
 */
public class Token {
    @Expose
    private final String token;
    @Expose
    private final Date expirationDate;
    /**
     * Token laeuft nach einer Stunde ab und muss erneuert werden
     */
    private static final long EXPIRATION_TIME = 1000*60*60;

    /**
     * Generiert einen zufaelligen Token inkl. Ablaufszeit
     */
    Token() {
        //Token-Generierung erfolgt per Zufall
        SecureRandom randomGenerator = new SecureRandom();
        byte[] randomness = new byte[2048];
        randomGenerator.nextBytes(randomness);
        //Im Original wird Charsets aus google.common.base verwendet
        this.token = new String(randomness, StandardCharsets.UTF_8);
        this.expirationDate = new Date(new Date().getTime() + EXPIRATION_TIME);
    }

    public String getToken() {
        return token;
    }

    boolean isExpired() {
        return new Date().after(expirationDate);
    }
}
