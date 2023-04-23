package api.messages.matches;

import api.messages.Message;
import com.google.gson.annotations.Expose;
import race.Race;

/**
 * Wird fuer Nachrichten verwendet, die Race-Informationen beinhalten.
 */
public class RaceMessage extends Message {
    @Expose
    private final Race game;

    public RaceMessage(Race game) {
        this.game = game;
    }
}
