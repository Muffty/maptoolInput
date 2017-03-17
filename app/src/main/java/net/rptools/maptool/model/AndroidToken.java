package net.rptools.maptool.model;

import com.bitfighters.maptool.maptoolinput.Connector;
import com.bitfighters.maptool.maptoolinput.MyData;

import java.util.Map;
import java.util.Set;

import net.rptools.lib.MD5Key;

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

    public MD5Key charsheetImage;
    public MD5Key portraitImage;


    public boolean isVisible = true;
    public boolean visibleOnlyToOwner = false;

    public String name;
    public Set<String> ownerList;

    public Map<String, MD5Key> imageAssetMap;
    public Map<String, Boolean> state;
    public Map<String, String> properties;

    public boolean pc;

    public String layer;

    public AndroidToken(){}

    public void LoadImages() {
        if(isVisible && layer.equals("TOKEN")){
            CheckLoad(portraitImage);
            CheckLoad(charsheetImage);
            CheckLoad(imageAssetMap.get(null));
        }
    }

    private void CheckLoad(MD5Key image) {
        if(image != null && MyData.instance.getBitmap(image) == null && !MyData.instance.loading(image)){
            MyData.instance.notifyLoad(image);
            Connector.currentConnection.LoadAsset(image);
        }
    }
}
