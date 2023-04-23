package race;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.MAP_DIR;

/**
 * Beinhaltet alle notwendigen Informationen eines Spielfeldes.
 */
public class RaceMap extends File {
    @Expose
    private final int mapID;
    @Expose
    private String mapName;
    @Expose
    private int numPlayers;
    @Expose
    private int length;
    @Expose
    private int height;
    @Expose
    private int[][] board;
    @Expose
    private int num_checkpoints;
    @Expose
    private HashMap<Integer, List<GamePosition>> checkpoints;
    @Expose
    private int num_startpoints;
    @Expose
    private List<GamePosition> startpoints;
    @Expose
    public boolean broken = false;
    @Expose
    private int gameDurationInSec;
    @Expose
    private int gameMode;
    @Expose
    private int laps;
    @Expose
    private String mapTxtName;

    private final static Logger logger = Logger.getLogger("GameMap");

    /**
     * Initialisiert alle Parameter, indem die angegebene Datei eingelesen wird.
     * @param mapID Zuzuordnende Map-ID
     * @param path Datei-Pfad des Spielfeldes
     * @throws Exception IOException
     */
    public RaceMap(int mapID, String path) throws Exception {
        super(path);

        if (!exists()) throw new IOException("File " + path + " does not exist");

        this.mapID = mapID;
        this.mapName = super.getName();

        readConfig(path);
        //readCompleteMap(path);
        if(this.checkpoints == null || this.startpoints == null || this.board == null) {
            logger.log(Level.WARNING, "The map is not a valid map. It needs the following structure:\n" +
                    RacegameConstants.MAP_IDENTIFIER + " - Identifies the section of the map\n" +
                    RacegameConstants.STARTPOINT_IDENTIFIER + " - Identifies the section of the startpositions\n" +
                    RacegameConstants.CHECKPOINT_IDENTIFIER + " - Identifies the section of the checkpoints\n");
            broken = true;
            return;
        }
        this.num_checkpoints = this.checkpoints.size();
        this.num_startpoints = this.startpoints.size();
    }

    /**
     * Liest die Konfigurationsdatei des Spielfeldes ein.
     * @param path Datei-Pfad der Map-Konfig.
     */
    public void readConfig(String path) {
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(path));
            Map<?, ?> map = gson.fromJson(reader, Map.class);

            this.numPlayers = Integer.parseInt(map.get("maxAnzahlSpieler").toString());
            this.gameMode = ((Double)map.get("spielmodus")).intValue();
            this.gameDurationInSec = Integer.parseInt(map.get("spiellaenge").toString());
            //TODO: Max Rundenanzahl in config angeben?
            this.laps = Integer.parseInt("0");
            mapTxtName = (String) map.get("spielfeld");
            readCompleteMap(mapTxtName);

            //TODO: read tile infos etc.
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Einlesen der kompletten Konfigurationsdatei eines Spielfeldes.
     * @param path Pfad zur Txt-Datei
     */
    public void readCompleteMap(String path) {
        try {
            //LadeKartenInformation
            InputStream is = new FileInputStream(MAP_DIR + "/" +path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while (br.ready()) {
                line = br.readLine();
                //Einlesen des Spielfeldes
                if (line.contentEquals(RacegameConstants.MAP_IDENTIFIER)) {
                    line = readMap(br);
                }

                //Einlesen der Startpunkte
                if (line.contentEquals(RacegameConstants.STARTPOINT_IDENTIFIER)) {
                    line = readStartpoints(br);
                }

                //Einlesen der Checkpunkte
                if (line.contentEquals(RacegameConstants.CHECKPOINT_IDENTIFIER)) {
                    readCheckpoints(br);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Liest den Abschnitt der Checkpunkte ein.
     * @param br
     * @return Aktuelle Zeile der Configurationsdatei.
     * @throws IOException Falls ein Fehler beim Lesen der naechsten Zeile auftritt.
     */
    private String readCheckpoints(BufferedReader br) throws IOException {
        checkpoints = new HashMap<>();
        String line;
        while (true) {
            line = br.readLine();
            if(line == null) {
                break;
            }
            String numbers[] = line.split(" ");
            int intnumbers[] = new int[numbers.length];
            for (int i = 0; i < numbers.length; i++) { //Lese das Array aus in ein Int Array
                intnumbers[i] = Integer.parseInt(numbers[i]);
            }
            //Schaffe Liste und setzte checkpoints
            List<GamePosition> checkList;
            if (!checkpoints.containsKey(intnumbers[2])) {
                checkList = new ArrayList<>();
            } else {
                //Fuege Element der Checkpointliste hinzu
                checkList = checkpoints.get(intnumbers[2]);
            }
            checkList.add(new GamePosition(intnumbers[0], intnumbers[1]));
            checkpoints.put(intnumbers[2], checkList);
        }
        return null;
    }

    /**
     * Liest den Abschnitt der Startpunkte ein.
     * @param br
     * @return Die aktuelle Zeile der Konfigurationsdatei.
     * @throws IOException Falls ein Fehler beim Lesen der naechsten Zeile auftritt.
     */
    private String readStartpoints(BufferedReader br) throws IOException {
        startpoints = new LinkedList<>();
        String line;
        while (true) {
            line = br.readLine();
            if(line == null) {
                break;
            }
            String numbers[] = line.split(" ");
            try {
                int col_pos = Integer.parseInt(numbers[0]);
                int row_pos = Integer.parseInt(numbers[1]);
                int direction = Integer.parseInt(numbers[2]);

                String s_direction = RacegameConstants.convertToDirection(direction);

                startpoints.add(new GamePosition(col_pos, row_pos, s_direction));
            } catch(NumberFormatException e) {
                //Next entry is CHECKPOINTS
                break;
            }
        }
        return line;
    }

    /**
     * Liest den Abschnitt des Spielfeldes ein.
     * @param br
     * @return Aktuelle Zeile der Konfigurationsdatei.
     * @throws IOException Falls ein Fehler beim Lesen der naechsten Zeile auftritt.
     */
    private String readMap(BufferedReader br) throws IOException {
        String line = br.readLine();
        String numb[] = line.split(" ");

        //Einlesen der Spaltenanzahl und Zeilenanzahl
        Object obj[] = new Object[numb.length];
        for (int i = 0; i < 2; i++) {
            obj[i] = Integer.parseInt(numb[i]);
        }
        this.length = (int) obj[0];
        this.height = (int) obj[1];
        this.board = new int[this.length][this.height];

        int col = 0;
        int row = 0;
        while (col < this.length && row < this.height) {
            line = br.readLine();
            while (col < this.length) {
                String numbers[] = line.split(" ");
                board[col][row] = Integer.parseInt(numbers[col]);
                col++;
            }
            if (col == this.length) {
                col = 0;
                row++;
            }
        }
        return line;
    }


    public String getMapName() {
        return mapName;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }

    public int getID() {
        return mapID;
    }

    public int getNum_checkpoints() {
        return num_checkpoints;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public String getMapTxtName() {
        return mapTxtName;
    }

    public int getGameDurationInSec() {
        return gameDurationInSec;
    }
}
