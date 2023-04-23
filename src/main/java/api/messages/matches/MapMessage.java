package api.messages.matches;

import api.messages.Message;
import com.google.gson.annotations.Expose;
import race.RaceMap;

import static api.Constants.CODE_MAP;

/**
 * Sendet die Metadaten eines Spielfeldes.
 */
public class MapMessage extends Message {
    @Expose
    private final RaceMap map;

    public MapMessage(RaceMap map) {
        super(CODE_MAP);
        this.map = map;
    }
}
