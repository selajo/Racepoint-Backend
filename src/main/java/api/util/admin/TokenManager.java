package api.util.admin;

import api.messages.admin.TokenizedMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.lang.NonNull;
import util.HMAC;
import util.SecurityIncidents;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.reflect.TypeToken;

import static util.FileSystemConstants.HOME_DIR;

/**
 * Verwaltet die Token, die aktuell in einer Session verwendet werden.
 */
public class TokenManager {
    private static final TokenManager instance = new TokenManager();
    Logger logger = Logger.getLogger("TokenManager");
    private final Map<String, Token> tokens;
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();


    private TokenManager() {
        tokens = new HashMap<>();
    }

    public static TokenManager getInstance() {
        return instance;
    }

    /**
     * Prueft, ob eine Nachricht fuer einen eingeschraenkten REST-API-Request gueltig ist.
     * @param message Die Nachricht, die einen Nutzernamen und ein Token enthaelt.
     * @param remoteIP Die IP des Systems der einkommenden Nachricht.
     * @return Ob die Kombination aus Nutzername und Token valide und nicht abgelaufen ist.
     */
    public boolean isTokenValid(TokenizedMessage message, String remoteIP) {
        return isTokenValid(message.getUserName(), message.getToken(), remoteIP);
    }

    /**
     * Peuueft, ob Nutzername und Token im System vorhanden sind und ob der Token abgelaufen ist.
     * @param userName Nutzername
     * @param token Token
     * @param remoteIP IP der Nutzers
     * @return True: Nutzername und Token sind im System vorhanden, Token ist nicht abgelaufen. False: Sonst
     */
    private boolean isTokenValid(String userName, String token, String remoteIP) {
        if(tokens.containsKey(userName)) {
            Token savedToken = tokens.get(userName);
            if(savedToken != null) {
                if(savedToken.isExpired() && token.contentEquals(savedToken.getToken())) {
                    logger.log(Level.INFO, "Tried to use expired login token. Remote IP is " + remoteIP);
                    SecurityIncidents.addSecurityEvent("Used expired, but correct token in admin panel request" +
                            " used for username " + userName + ". Remote IP was " + remoteIP, false);
                    return false;
                } else {
                    if(!savedToken.isExpired() && token.contentEquals(savedToken.getToken())) {
                        return true;
                    } else {
                        logger.log(Level.WARNING, "Invalid token in admin panel request used for username "
                                + userName + "\nRemote IP was " + remoteIP);
                        SecurityIncidents.addSecurityEvent("Invalid token in admin panel request used for" +
                                " username " + userName + ". Remote IP was " + remoteIP, true);
                        return false;
                    }
                }
            } else {
                SecurityIncidents.addSecurityEvent("Gave token in admin panel request used for username " + userName +
                        ", but could not retrieve it. Remote IP was " + remoteIP, true);
                return false;
            }
        } else {
            SecurityIncidents.addSecurityEvent("Gave token in admin panel request used for username " + userName +
                    ", but there is none stored for the user. Remote IP was " + remoteIP, true);
            return false;
        }
    }

    /**
     * Hasht ein uebertragenes Passwort mithilfe der password_hash_key-Datei.
     * @param password Das uebermittelte Passwort fuer einen Login-
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String hashKey = Files.readString(Paths.get(HOME_DIR + "/password_hash_key.txt")).trim();
        return HMAC.calculateHMAC(password, hashKey);
    }

    /**
     * Generiert einen Token fuer einen Login-Versuch.
     * @param userName Der verwendete Nutzername.
     * @param passWord Das verwendete Passwort.
     * @return Einen Token, falls die Authorisierung erfolgreich war.
     * @throws AuthException Falls die Authorisierung fehlgeschlagen ist.
     */
    @NonNull
    public String authenticateUser(String userName, String passWord) throws AuthException {
        try {
            String passWordHash = hashPassword(passWord);
            Path keys = Paths.get(HOME_DIR + "/admin_keys.json");
            Type type = new TypeToken<ArrayList<PasswordHashEntry>>(){}.getType();
            ArrayList<PasswordHashEntry> hashList = gson.fromJson(Files.readString(keys), type);
            for(PasswordHashEntry entry : hashList) {
                if(entry.getUsername().contentEquals(userName) && entry.getHash().contentEquals(passWordHash)) {
                    Token token = new Token();
                    tokens.put(userName, token);
                    return token.getToken();
                }
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, "Could not authenticate user! Something went wrong: " + e.getLocalizedMessage(), e);
            throw new AuthException("Could not authenticate user! Something went wrong: " + e.getLocalizedMessage());
        }
        throw new AuthException("Username or password invalid");
    }

}
