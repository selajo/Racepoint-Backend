package api.messages;

import com.google.gson.annotations.Expose;

/**
 * Beinhaltet die Struktur einer Nachricht, die an das Frontend versendet werden kann.
 */
public abstract class Message {

    @Expose
    private final int code;

    @Expose
    private final String message;

    public Message() {
        this.code = -1;
        this.message = "";
    }
    public Message(int code) {
        this.code = code;
        this.message = "";
    }

    public Message(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() { return message; }
}
