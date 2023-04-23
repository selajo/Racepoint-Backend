package api.messages.matches;

import api.messages.Message;
import com.google.gson.annotations.Expose;
import race.RaceMap;

import java.util.List;

import static api.Constants.CODE_MAP;

/**
 * Wird fuer Nachrichten verwendet, die mehrere Maps beinhaltet.
 */
public class MultipleMapsMessage extends Message {
    @Expose
    private final List<RaceMap> maps;

    public MultipleMapsMessage(List<RaceMap> maps) {
        super(CODE_MAP);
        this.maps = maps;
    }

}
