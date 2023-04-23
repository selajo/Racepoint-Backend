package distributor.messages;

import com.google.gson.annotations.Expose;

public class MapDataMessage extends Message {

    @Expose
    private final int mapID;

    @Expose
    private final String fileName;

    @Expose
    private final byte[] mapData;

    public MapDataMessage(int mapID, String fileName, byte[] mapData) {
        this.mapID = mapID;
        this.fileName = fileName;
        this.mapData = mapData;
    }

    public int getMapID() {
        return mapID;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getMapData() {
        return mapData;
    }
}
