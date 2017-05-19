package com.bitfighters.maptool.maptoolinput.Dialogs;

import android.graphics.Bitmap;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.GUID;

/**
 * Created by Tobias on 19.05.2017.
 */

public class MapToolDialog {

    private AndroidToken token;
    private String message;
    private String fromPlayer, overrideFrom;
    private boolean allowReply;
    private String positiveButton, neutralButton, negativeButton;

    public MapToolDialog(AndroidToken token, String message, boolean allowReply, String positiveButton, String neutralButton, String negativeButton, String fromPlayer, String overrideFrom){
        this.token = token;
        this.message = message;
        this.allowReply = allowReply;
        this.positiveButton = positiveButton;
        this.neutralButton = neutralButton;
        this.negativeButton = negativeButton;
        this.fromPlayer = fromPlayer;
        this.overrideFrom = overrideFrom;
    }

    public AndroidToken getToken() {
        return token;
    }

    public String getNegativeButton() {
        return negativeButton;
    }

    public String getNeutralButton() {
        return neutralButton;
    }

    public String getPositiveButton() {
        return positiveButton;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAllowReply() {
        return allowReply;
    }

    public String getFromPlayer() {
        return fromPlayer;
    }

    public String getOverrideFrom() {
        return overrideFrom;
    }
}
