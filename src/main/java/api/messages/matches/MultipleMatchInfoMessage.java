package api.messages.matches;

import api.messages.Message;
import com.google.gson.annotations.Expose;
import util.matches.MatchListInfo;
import util.matches.ShortMatchInfo;

import java.util.ArrayList;

import static api.Constants.CODE_MATCH_LIST;

/**
 * Wird fuer Nachrichten verwendet, die mehrere Match-Infos beinhalten
 */
public class MultipleMatchInfoMessage extends Message {
    @Expose
    private final MatchListInfo shortMatchInfos;

    public MultipleMatchInfoMessage(MatchListInfo shortMatchInfos) {
        super(CODE_MATCH_LIST);
        this.shortMatchInfos = shortMatchInfos;
    }
}
