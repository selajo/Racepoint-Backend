package api.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.messages.admin.GetSettingsMessage;
import api.messages.Message;
import util.BuildMessageException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static util.FileSystemConstants.LOG_DIR;

/**
 * Ist fuer das Schicken der Nachrichten zustaendig, die mit dem Archive arbeiten.
 * URL/archive/...
 */
@RestController
public class ArchiveController {

    /**
     * Zippt und versendet die Log-Dateien eines beliebigen Matches via ID.
     * @param request Anfragerequest
     * @param response Anfrageresponse
     * @param matchID Zu holendes Match
     * @return Nachricht mit Zip-Datei
     * @throws BuildMessageException Fehler beim Bauen der Zip-Datei
     */
    @RequestMapping("/log/{matchID}")
    public ResponseEntity<Resource> logFile(HttpServletRequest request,
                                            HttpServletResponse response, @PathVariable long matchID) throws BuildMessageException {
        File logFile = Paths.get(LOG_DIR.getAbsolutePath() + "/" + matchID + ".7z").toFile();
        if (logFile.exists()) {
            InputStreamResource resource = null;
            try {
                resource = new InputStreamResource(new FileInputStream(logFile));
            } catch (FileNotFoundException e) {
                throw new BuildMessageException("Error reading log file: " + e.getLocalizedMessage());
            }
            return ResponseEntity.ok()
                    .contentLength(logFile.length())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        } else {
            throw new BuildMessageException("Log file not found");
        }
    }
}
