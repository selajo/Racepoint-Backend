package api.controller;

import api.messages.Message;
import api.messages.matches.RaceMessage;
import api.messages.matches.MultipleGamesMessage;
import matchstarting.ControlManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Verwaltet Anfragen zu den einzelnen Races
 */
@RestController
public class RaceController {
    /**
     * Liefert Informationen zu dem aktuellen Race
     * @return Informationen zu einem Race
     * @throws Exception Auftretender Fehler beim Einlesen der Map
     */
    @RequestMapping(value = "/race/current", produces = APPLICATION_JSON_VALUE)
    public Message getCurrentGame() throws Exception {
        return new RaceMessage(ControlManager.getInstance().getCurrentGame());
    }

    /**
     * Liefert Informationen zu den abgeschlossenen Races des aktuellen Matches
     * @return Informationen zu den abgeschlossenen Races des aktuellen Matches
     * @throws Exception Auftretender Fehler beim Einlesen der Map
     */
    @RequestMapping(value = "/race/completed", produces = APPLICATION_JSON_VALUE)
    public Message getCompletedGames() throws Exception {
        return new MultipleGamesMessage(ControlManager.getInstance().getCurrentMatch().getCompletedRaces());
    }

    /**
     * Liefert Informationen zu den noch zu spielenden Races des aktuellen Matches
     * @return Informationen zu den noch zu spielenden Races des aktuellen Matches
     * @throws Exception Auftretender Fehler beim Einlesen der Map
     */
    @RequestMapping(value = "/race/remaining", produces = APPLICATION_JSON_VALUE)
    public Message getRemainingGames() throws Exception {
        return new MultipleGamesMessage(new ArrayList<>(ControlManager.getInstance().getRemainingGames()));
    }

}
