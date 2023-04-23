package api.messages.admin;

/**
 * Authentisiert einen Admin-Panel-Nutzer.
 * Wird via REST-API mit POST erhalten.
 */
public class AuthenticationMessage {
    private String userName;
    private String password;

    public AuthenticationMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public AuthenticationMessage() {

    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
