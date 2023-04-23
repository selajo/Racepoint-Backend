package api.messages.matches;

import api.messages.Message;
import com.google.gson.annotations.Expose;
import race.Race;
import java.util.ArrayList;

import static api.Constants.CODE_RACE;

/**
 * Wird fuer Nachrichten verwendet, die mehrere Matches verwendet
 */
public class MultipleGamesMessage extends Message {
    @Expose
    private final ArrayList<Race> games;

    public MultipleGamesMessage(ArrayList<Race> games) {
        super(CODE_RACE);
        this.games = games;
    }
}
