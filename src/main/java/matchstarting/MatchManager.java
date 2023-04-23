package matchstarting;


import race.Match;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Beinhaltet alle Informationen fuer das Planen eines Matches und setzt einen Timer.
 * Singleton.
 */
public class MatchManager {
    private static MatchManager instance = new MatchManager();
    public static MatchManager getInstance() { return instance; }

    private Logger logger = Logger.getLogger("MatchManager");
    private Timer timer;


    ArrayList<String> allowedMaps = new ArrayList<>();
    ArrayList<Integer> allowedClientIDs = new ArrayList<>();
    ArrayList<String> allowedClientNames = new ArrayList<>();

    /**
     * Startet den MatchManager und initialisiert den Timer.
     */
    public void start() {
        logger.log(Level.INFO, "MatchManager started");
        setTimer();
    }

    /**
     * Setzt alle erlaubten Clients und Spielfelder
     * @param allowedMaps Aller erlaubten Maps
     * @param allowedClientIDs Alle erlaubten Clients via ID
     * @param allowedClientNames Alle erlaubten Clients via Namen
     */
    public void setAllowedThings(ArrayList<String> allowedMaps, ArrayList<Integer> allowedClientIDs, ArrayList<String> allowedClientNames) {
        this.allowedClientNames = allowedClientNames;
        this.allowedMaps = allowedMaps;
        this.allowedClientIDs = allowedClientIDs;
    }

    /**
     * Setzt den Timer fuer das Match-Setup und plant das Match
     */
    void setTimer() {
        logger.log(Level.INFO, "Set match scheduling timer");
        //Alten Timer beenden, falls existent
        if(timer != null) {
            try {
                timer.cancel();
                timer.purge();
            } catch (IllegalStateException e) {
                logger.log(Level.WARNING, "Failed to set match scheduling timer: " + e.getLocalizedMessage(), e);
            }
        }

        //Neuen Timer setzen
        timer = new Timer();
        Properties properties = RacepointProperties.readProperties();
        boolean continuous = Boolean.parseBoolean(properties.getProperty(RacepointProperties.CONTINUOUS_MODE, "false"));
        if(continuous) {
            logger.log(Level.INFO, "Starting in continuous mode");
            long periodMillis = Long.parseLong(properties.getProperty(RacepointProperties.MATCH_PERIOD_IN_MIN)) * 60 * 1000;
            long delay = findRelativeStartTimeDelay(properties);
            timer.scheduleAtFixedRate(new MatchSetup(), delay, periodMillis);
            logger.log(Level.INFO, "Next match scheduled in " + delay / (60 * 1000) + " minutes");
        } else {
            logger.log(Level.INFO, "Single match mode");
            timer.schedule(new MatchSetup(true), 0);
        }
    }

    /**
     * Ermittelt die Startzeiten in Abhaengigkeit vom heutigen Mitternacht fuer den CONITINUOUS_MODE.
     * @param properties Die Match-Properties.
     * @return Die Verzögerung bis zum naechten Match-Start.
     */
    private long findRelativeStartTimeDelay(Properties properties) {
        long intervalMinutes = Integer.parseInt(properties.getProperty(RacepointProperties.MATCH_PERIOD_IN_MIN));
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDateTime matchStartTime = LocalDateTime.of(today, midnight);
        LocalDateTime current = LocalDateTime.now(ZoneId.systemDefault());
        while(matchStartTime.isBefore(current)) {
            matchStartTime = matchStartTime.plusMinutes(intervalMinutes);
        }
        Date matchStart = Date.from(matchStartTime.atZone(ZoneId.systemDefault()).toInstant());
        return matchStart.getTime() - System.currentTimeMillis();
    }

    /**
     * Stoppt das aktuelle Match und startet ein neues.
     */
    public void stopStartMatch() {
        forceEndMatchManual();
        logger.log(Level.INFO, "Requested new match in admin interface");
        setTimer();
        //timer.schedule(new MatchSetup(true), 0);
    }

    /**
     * Erzwingt ein manuelles Abbrechen von einem Match.
     */
    public void forceEndMatchManual() {
        logger.log(Level.INFO, "Requested forced match end in matchstarting.MatchManager by admin");
        Match match = ControlManager.getInstance().getCurrentMatch();
        if(match != null) {
            if(match.getMatchPhase() != Match.MatchPhase.COMPLETED) {
                ControlManager.getInstance().interruptGame();
                match.setError();
                match.endMatch();
            }
        }
        //setTimer();
    }

    /**
     * Erzwingt das Beenden eines Matches wegen Ablauf des Timers.
     */
    void forceEndMatchTime() {
        logger.log(Level.INFO, "Requested forced match end in MatchManager due to time problems");
        Match match = ControlManager.getInstance().getCurrentMatch();
        ControlManager.getInstance().interruptGame();
        match.setTimeOut();
        match.endMatch();
    }

    /**
     * Prueft, ob ein Timeout beim aktuellen Match aufgetreten ist und beendet dieses ggf.
     * @return Ob ein Timeout aufgetreten ist.
     */
    boolean checkForTimeout() {
        logger.log(Level.INFO, "Checked for timeout on current match");
        Match match = ControlManager.getInstance().getCurrentMatch();
        return match.checkForTimeOut();
    }

    public ArrayList<Integer> getAllowedClientIDs() {
        return allowedClientIDs;
    }

    public ArrayList<String> getAllowedClientNames() {
        return allowedClientNames;
    }

    public ArrayList<String> getAllowedMaps() {
        return allowedMaps;
    }
}
