package api.messages.admin;

import api.messages.Message;

import static api.Constants.CODE_TOKEN;

/**
 * Sendet einen Authorisierungs-Token an das Admin-Panel nach einem erfolgreichen Login.
 */
public class TokenMessage extends Message {
    private String token;

    public TokenMessage(String token) {
        super(CODE_TOKEN);
        this.token = token;
    }
}
