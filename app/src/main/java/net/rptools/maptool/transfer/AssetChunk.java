package net.rptools.maptool.transfer;

import java.io.Serializable;

/**
 * Created by admin on 10.03.2017.
 */


public class AssetChunk implements Serializable {
    private Serializable id;
    private byte[] data;

    public AssetChunk(Serializable id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public Serializable getId() {
        return id;
    }
}
