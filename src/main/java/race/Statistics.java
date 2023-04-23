package race;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * Beinhaltet die Statistiken des Matches inkl. Statistiken der einzelnen Races.
 */
public class Statistics {
    @Expose
    public List<PlayerData> Spielerdaten;

    @Expose
    public String StartTimestamp;

    @Expose
    public String EndTimestamp;

    @Expose
    public int Spielmodus;

    @Expose
    public int gameID;

    @Expose
    public int mapID;

    private final static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();


    public Statistics(List<PlayerData> Spielerdaten, String StartTimeStamp, String EndTimeStamp, int Spielmodus) {
        this.EndTimestamp = EndTimeStamp;
        this.Spielerdaten = Spielerdaten;
        this.Spielmodus = Spielmodus;
        this.StartTimestamp = StartTimeStamp;
    }

    /**
     * Liest die Statistik-Datei ein.
     * @param file Dateipfad zur Statistik-Datei
     * @return Statistiken
     * @throws FileNotFoundException
     */
    static Statistics readStatistics(String file) throws FileNotFoundException {
        if(file == null) {
            return null;
        }
        Reader reader = new FileReader(file);
        return gson.fromJson(reader, Statistics.class);
    }

    /**
     * Konvertiert die Statistiken zu einer Zeichenkette
     * @return Statistik in Form einer Zeichenkette
     */
    @Override
    public String toString() {
        return  "Spielerdaten: " + Spielerdaten + "\n" +
                "Spielmodus: " + Spielmodus + "\n" +
                "StartTime: " + StartTimestamp + "\n" +
                "EndTime: " + EndTimestamp;
    }
}

/**
 * Struktur der Statistiken, die das Rennspiel nach Spielende speichert.
 */
class PlayerData {
    @Expose
    public int TileKollisionAnzahl;
    @Expose
    public int Spielernummer;
    @Expose
    public Map<String, Float> Rundenzeiten;
    @Expose
    public int Endplatzierung;
    @Expose
    public int SpielerKollisionAnzahl;
    @Expose
    public int Rundenanzahl;
    @Expose
    public float BesteRundenZeit;

}
