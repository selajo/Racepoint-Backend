package race;

import com.google.gson.annotations.Expose;

/**
 * Stellt eine Position des Spielfeldes dar.
 * Beinhaltet Koordinaten und Richtung.
 */
public class GamePosition {
    @Expose
    public int col_position;
    @Expose
    public int row_position;
    @Expose
    public String direction;

    public GamePosition(int col_position, int row_position, String direction) {
        this.col_position = col_position;
        this.row_position = row_position;
        this.direction = direction;
    }

    public GamePosition(int col_position, int row_position) {
        this.col_position = col_position;
        this.row_position = row_position;
        this.direction = "n/a";
    }

}
