package net.rptools.maptool.model;

public class AndroidTokenState {

    /**
     * The name of this overlay. Normally this is the name of a state.
     */
    public String name;

    /**
     * Order of the states as displayed on the states menu.
     */
    public int order;

    /**
     * The group that this token overlay belongs to. It may be <code>null</code>.
     */
    public String group;

    /**
     * Flag indicating that this token overlay is only displayed on mouseover
     */
    public boolean mouseover;

    /**
     * The opacity of the painting. Must be a value between 0 & 100
     */
    public int opacity = 100;

    /**
     * Flag indicating that this token overlay is displayed to the user.
     */
    public boolean showGM;

    /**
     * Flag indicating that this token overlay is displayed to the owner.
     */
    public boolean showOwner;

    /**
     * Flag indicating that this token overlay is displayed to the everybody else.
     */
    public boolean showOthers;

    public AndroidTokenState(){}

    public AndroidTokenState(String name, int order, String group, boolean mouseover, int opacity, boolean showGM,
                             boolean showOwner, boolean showOthers) {
        super();
        this.name = name;
        this.order = order;
        this.group = group;
        this.mouseover = mouseover;
        this.opacity = opacity;
        this.showGM = showGM;
        this.showOwner = showOwner;
        this.showOthers = showOthers;
    }
}
