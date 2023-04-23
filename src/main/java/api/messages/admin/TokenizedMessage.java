package api.messages.admin;

/**
 * Wird fuer den Admin-Zugriff verwendet.
 * Ein Token beinhaltet einen Usernamen.
 */
public class TokenizedMessage {
    private String userName;
    private String token;

    public String getUserName() {
        return userName;
    }

    public String getToken() {
        return token;
    }

    public TokenizedMessage(String userName, String token) {
        this.userName = userName;
        this.token = token;
    }
}
