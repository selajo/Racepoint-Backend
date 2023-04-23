package api.messages.matches;

import api.messages.Message;
import com.google.gson.annotations.Expose;
import race.Match;

import static api.Constants.CODE_MATCH;

/**
 * Wird fuer das Versenden von Match-Nachrichten verwendet
 */
public class MatchMessage extends Message {
    @Expose
    private final Match match;

    public MatchMessage(Match match) {
        super(CODE_MATCH);
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }
}
