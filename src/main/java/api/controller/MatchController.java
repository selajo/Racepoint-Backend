package api.controller;

import api.messages.ErrorMessage;
import api.messages.Message;
import api.messages.matches.MatchMessage;
import api.messages.matches.MultipleMatchInfoMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import race.Match;
import matchstarting.ControlManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.matches.MatchListInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static util.FileSystemConstants.*;

/**
 * Verwaltet Anfragen zu den Matches
 */
@RestController
public class MatchController {
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Liefert Informationen zum aktuell ausgefuehrten Match
     * @return Informationen zum aktuell ausgefuehrten Match
     * @throws Exception Aufgetretener Fehler
     */
    @RequestMapping(value = "/match/current", produces = APPLICATION_JSON_VALUE)
    public Message getCurrent() throws Exception {
        return new MatchMessage(ControlManager.getInstance().getCurrentMatch());
    }

    /**
     * Liefert Informationen zu allen Matches, die stattgefunden haben
     * @return Informationen zu allen Matches, die stattgefunden haben
     */
    @RequestMapping(value = "/match/all", produces = APPLICATION_JSON_VALUE)
    public Message getMatchInfos() {
        try {
            Reader reader = new FileReader(matchListInfo);
            MatchListInfo shortMatchInfos = gson.fromJson(reader, MatchListInfo.class);

            return new MultipleMatchInfoMessage(shortMatchInfos);
        } catch (FileNotFoundException e) {
            return new ErrorMessage("Could not find recent match infos");
        }
    }

    /**
     * Liefert Informationen zu einem Match via ID
     * @param request Anfragerequest
     * @param response Anfrageresponse
     * @param matchID Match-ID
     * @return Informationen zu einem Match
     */
    @RequestMapping("/match/{matchID}")
    public Message getMatchWithID(HttpServletRequest request,
                                            HttpServletResponse response, @PathVariable long matchID) {
        try {
            Reader reader = new FileReader(MATCH_HOME.getAbsolutePath() + "/" + matchID + "/matchInfo.json");
            Match match = gson.fromJson(reader, Match.class);
            return new MatchMessage(match);
        } catch (FileNotFoundException e) {
            return new ErrorMessage("Could not find match infos for the given matchID " + matchID);
        }
    }

}
