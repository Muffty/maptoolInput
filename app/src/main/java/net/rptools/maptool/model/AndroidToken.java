package net.rptools.maptool.model;

import java.util.Set;

/**
 * Created by me on 01.03.2017.
 */
public class AndroidToken {
    public GUID id;
    public boolean beingImpersonated ;
    public GUID exposedAreaGUID;




    public int x;
    public int y;
    public int z;

    public int anchorX;
    public int anchorY;

    public double sizeScale = 1;

    public int lastX;
    public int lastY;



    public boolean isVisible = true;
    public boolean visibleOnlyToOwner = false;

    public String name;
    public Set<String> ownerList;

    public AndroidToken(){}

    public AndroidToken(GUID id, boolean beingImpersonated, GUID exposedAreaGUID, int x, int y, int z, int anchorX, int anchorY, int lastX, int lastY, String name, Set<String> ownerList) {
        this.id = id;
        this.beingImpersonated = beingImpersonated;
        this.exposedAreaGUID = exposedAreaGUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.lastX = lastX;
        this.lastY = lastY;
        this.name = name;
        this.ownerList = ownerList;
    }
}
