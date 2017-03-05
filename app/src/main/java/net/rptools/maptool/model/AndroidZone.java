package net.rptools.maptool.model;

import java.util.Map;

public class AndroidZone {
    public GUID id = new GUID();
    public int gridOffsetX = 0;
    public int gridOffsetY = 0;
    public int gridSize;

    public Map<GUID, AndroidToken> tokenMap;

    public String name;
    public boolean isVisible;

    public AndroidZone(){  }

    public AndroidZone(GUID id, int gridOffsetX, int gridoffsetY, int gridSize,
                       Map<GUID, AndroidToken> tokenMap, String name, boolean isVisible) {
        this.id = id;
        this.gridOffsetX = gridOffsetX;
        this.gridOffsetY = gridoffsetY;
        this.gridSize = gridSize;
        this.name = name;
        this.isVisible = isVisible;
        this.tokenMap = tokenMap;
    }

    @Override
    public String toString() {
        return name + "["+id+"]";
    }
}
