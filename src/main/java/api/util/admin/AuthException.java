package api.util.admin;

/**
 * Signalisiert einen ungültigen Usernamen und Passort fuer einen Login.
 */
public class AuthException extends Exception {
    AuthException(String reason) {
        super(reason);
    }
}
