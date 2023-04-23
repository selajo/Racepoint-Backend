package api.controller;

import api.messages.ErrorMessage;
import api.messages.Message;
import api.messages.admin.*;
import api.util.admin.AuthException;
import api.util.admin.TokenManager;
import matchstarting.MatchManager;
import org.springframework.web.bind.annotation.*;
import util.SecurityIncidents;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static api.Constants.CODE_TOKEN_INVALID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static util.FileSystemConstants.*;

/**
 * Verwaltet die Anfragen der User auf Admin-Ebene. Benoetigt User-Authentifizierung.
 */
@RestController
public class AdminController {
    private final Logger logger = Logger.getLogger("AdminController");
    /**
     * Gespeicherte Login-Versuche
     */
    final List<Date> lastLoginAttempts = new ArrayList<>();

    /**
     * Prueft, ob der User eine Anzahl von fuenf fehlerhaften Anmeldeversuchen hatte.
     * @return True: Login-Versuche <= 5. False: Sonst
     */
    boolean notTooManyLoginAttempts() {
        lastLoginAttempts.add(new Date());
        if(lastLoginAttempts.size() > 5) {
            Date oldestLogin = lastLoginAttempts.get(0);
            lastLoginAttempts.remove(0);
            Date oneMinuteAgo = new Date(new Date().getTime() - 60*1000);
            return oldestLogin.before(oneMinuteAgo);
        } else {
            return true;
        }
    }

    /**
     * Request wird fuer Login und Generierung eines Admin-Panel-Tokens verwendet.
     * @param authMessage Besteht aus Username und Passwort fuer einen Login-Versuch.
     * @param request Anfragerequest
     * @return Eine TokenMessage mit einem generierten Token bei einem fehlerfreien Versuch. Ansonsten eine ErrorMessage.
     */
    @PostMapping(value = "/admin/login", produces = APPLICATION_JSON_VALUE)
    public Message login(@RequestBody AuthenticationMessage authMessage, HttpServletRequest request) {
        String remoteIP = "Forwarded: " + request.getHeader("X-Forwarded-For") + ", remote " + request.getRemoteAddr();
        if(notTooManyLoginAttempts()) {
            try {
                String token = TokenManager.getInstance().authenticateUser(authMessage.getUserName(), authMessage.getPassword());
                logger.log(Level.INFO, "Login attempt succeeded from " + remoteIP);
                SecurityIncidents.addSecurityEvent("Successful admin panel login attempt from "
                        + remoteIP, false);
                return new TokenMessage(token);
            } catch (AuthException e) {
                logger.log(Level.INFO, "Failed login attempt from " + remoteIP + ": " + e.getLocalizedMessage(), e);
                SecurityIncidents.addSecurityEvent("Failed admin panel login attempt! " + e.getMessage() +
                        ". Request was from " + remoteIP, true);
                return new ErrorMessage(e.getMessage());
            }
        } else {
            logger.log(Level.WARNING, "Stopped login attempt because there where too many in the last minute");
            SecurityIncidents.addSecurityEvent("Admin panel login attempt stopped, there were too many " +
                    "in the last time! Request was from " + remoteIP, true);
            return new ErrorMessage("Too many login attempts in the last time");
        }
    }

    /**
     * Beendet das aktuelle Match, falls eines aktiv ist. Anschliessen wird ein neues Match gestartet.
     * @param startMessage Beinhaltet alle Clients und Maps, die an dem Match teilnehmen sollen
     * @param request Eine Ack-Message, bei erfolgreicher Authorisierung, andernfalls eine Error-Message.
     * @return Antwort, ob Token valide ist
     */
    @PostMapping(value = "/admin/stopstart", produces = APPLICATION_JSON_VALUE)
    public Message stopStartMatch(@RequestBody StartMessage startMessage, HttpServletRequest request) {
        String remote = "Forwarded: " + request.getHeader("X-Forwarded-For") + ", remote " + request.getRemoteAddr();
        if(TokenManager.getInstance().isTokenValid(new TokenizedMessage(startMessage.getUserName(), startMessage.getToken()), remote)) {
            logger.log(Level.INFO, "Forced new match start from admin panel");
            MatchManager.getInstance().setAllowedThings(startMessage.getMapNames(), startMessage.getClientID(), startMessage.getClientNames());
            logger.log(Level.INFO, startMessage.getClientID().size() + " clients participate in this match");
            printAllowed();
            MatchManager.getInstance().stopStartMatch();
            return new AckMessage();
        } else {
            return new ErrorMessage("Token invalid, please log in again", CODE_TOKEN_INVALID);
        }
    }

    /**
     * Gibt die erlaubten Spielfelder und Clients aus.
     */
    private void printAllowed() {
        logger.log(Level.INFO, "Allowed Maps:");
        for(String raceMap : MatchManager.getInstance().getAllowedMaps()) {
            logger.log(Level.INFO, raceMap);
        }

        logger.log(Level.INFO, "Allowed Clients:");
        for(int i = 0; i < MatchManager.getInstance().getAllowedClientNames().size(); i++) {
            logger.log(Level.INFO, MatchManager.getInstance().getAllowedClientNames().get(i));
        }
    }

    /**
     * Stoppt ein Match, falls gerade eins laeuft.
     * @param token Token des Nutzers
     * @param request Zum Speichern der IPs
     * @return Ob der Token valide ist
     */
    @PostMapping( value = "/admin/stop", produces = APPLICATION_JSON_VALUE)
    public Message stopMatch(@RequestBody TokenizedMessage token, HttpServletRequest request) {
        String remote = "Forwarded: " + request.getHeader("X-Forwarded-For") + ", remote " + request.getRemoteAddr();
        if(TokenManager.getInstance().isTokenValid(token, remote)) {
            logger.log(Level.INFO, "Forced end match from admin panel");
            MatchManager.getInstance().forceEndMatchManual();
            return new AckMessage();
        } else {
            return new ErrorMessage("Token invalid, please log in again", CODE_TOKEN_INVALID);
        }
    }

    /**
     * Zum testweisen Starten der Matches mit allen Cients.
     * @param request Zum Speichenr der IPs
     * @return Ob der Token valide ist
     */
    @RequestMapping(value = "/test/start", produces = APPLICATION_JSON_VALUE)
    public Message stopStartMatch(HttpServletRequest request) {
        String remote = "Forwarded: " + request.getHeader("X-Forwarded-For") + ", remote " + request.getRemoteAddr();
        if(true) {
            logger.log(Level.INFO, "Forced new match start from admin panel");
            MatchManager.getInstance().stopStartMatch();
            return new AckMessage();
        } else {
            return new ErrorMessage("Token invalid, please log in again", CODE_TOKEN_INVALID);
        }
    }

    /**
     * Loescht alle gezippten Logs und alte MatchInfos
     * @param request Anfragerequest
     * @return Ob der Token valide ist
     */
    @RequestMapping(value = "/admin/delete", produces = APPLICATION_JSON_VALUE)
    //public Message deleteAll(@RequestBody TokenizedMessage token, HttpServletRequest request) {
    public Message deleteAll(HttpServletRequest request) {
        String remote = "Forwarded: " + request.getHeader("X-Forwarded-For") + ", remote " + request.getRemoteAddr();
        //if(TokenManager.getInstance().isTokenValid(token, remote)) {
        if(true) {
            logger.log(Level.INFO, "Deleting everything...");
            deleteFolderContent(LOG_DIR);
            deleteFolderContent(MATCH_HOME);
            return new AckMessage();
        } else {
            return new ErrorMessage("Token invalid, please log in again", CODE_TOKEN_INVALID);
        }
    }
}
