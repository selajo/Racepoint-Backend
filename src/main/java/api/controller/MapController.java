package api.controller;

import api.messages.ErrorMessage;
import api.messages.Message;
import api.messages.matches.MultipleMapsMessage;
import api.messages.matches.MapMessage;
import race.RaceMap;
import matchstarting.ControlManager;
import matchstarting.RaceMapSetup;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Verwaltet Anfragen zu den Spielfeldern
 */
@RestController
public class MapController {
    /**
     * Liefert Informationen zu einem Spielfeld via ID
     * @param id Map-ID
     * @return Informationen zu dem Spielfeld
     * @throws Exception Aufgetretener Fehler beim Einlesen der Maps
     */
    @RequestMapping(value = "/map/{id}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public Message getMap(@PathVariable("id") Integer id) throws Exception {
        ControlManager controlManager = ControlManager.getInstance();
        RaceMap map = controlManager.getMapWithID(id);
        if (map != null) {
            return new MapMessage(map);
        } else {
            return new ErrorMessage("There is no gamemap with ID " + id + " in the current match");
        }
    }

    /**
     * Holt Informationen zu allen Spielfeldern, die sich auf dem System befinden.
     * @return Informationen zu allen Spielfeldern
     * @throws Exception Aufgetretener Fehler beim Einlesen der Maps
     */
    @RequestMapping(value = "/map/all", produces = APPLICATION_JSON_VALUE)
    public MultipleMapsMessage getAllMaps() throws Exception {
        //Noch keine GameMaps eingelesen -> jetzt alle möglichen einlesen
        return new MultipleMapsMessage(RaceMapSetup.readGameMaps(true));
    }

}
