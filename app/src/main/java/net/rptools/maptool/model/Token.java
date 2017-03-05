/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.rptools.CaseInsensitiveHashMap;
import net.rptools.lib.MD5Key;

/**
 * This object represents the placeable objects on a map. For example an icon that represents a character would exist as
 * an {@link Asset} (the image itself) and a location and scale.
 */
public class Token extends BaseModel {

	public GUID id = new GUID();

	public static final String FILE_EXTENSION = "rptok";
	public static final String FILE_THUMBNAIL = "thumbnail";

	public static final String NAME_USE_FILENAME = "Use Filename";
	public static final String NAME_USE_CREATURE = "Use \"Creature\"";

	public static final String NUM_INCREMENT = "Increment";
	public static final String NUM_RANDOM = "Random";

	public static final String NUM_ON_NAME = "Name";
	public static final String NUM_ON_GM = "GM Name";
	public static final String NUM_ON_BOTH = "Both";

	private boolean beingImpersonated = false;
	private GUID exposedAreaGUID;

	public enum TokenShape {
		TOP_DOWN("Top down"), CIRCLE("Circle"), SQUARE("Square"), FIGURE("Figure");

		private String displayName;

		private TokenShape(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	public enum Type {
		PC, NPC
	}

	private Map<String, MD5Key> imageAssetMap;
	private String currentImageAsset;

	public int x;
	public int y;
	public int z;

	public int anchorX;
	public int anchorY;

	private double sizeScale = 1;

	public int lastX;
	public int lastY;
	private Path<? extends AbstractPoint> lastPath;

	private boolean snapToScale = true; // Whether the scaleX and scaleY represent snap-to-grid measurements

	// These are the original image width and height
	private int width;
	private int height;

	private double scaleX = 1;
	private double scaleY = 1;

	public Map<Class<? extends Grid>, GUID> sizeMap;

	private boolean snapToGrid = true; // Whether the token snaps to the current grid or is free floating

	private boolean isVisible = true;
	private boolean visibleOnlyToOwner = false;

	public String name;
	public Set<String> ownerList;

	public int ownerType;

	private static final int OWNER_TYPE_ALL = 1;
	private static final int OWNER_TYPE_LIST = 0;

	private String tokenShape;
	private String tokenType;
	private String layer;

	private String propertyType = "Basic";

	private Integer facing = null;

	private Integer haloColorValue;

	private Integer visionOverlayColorValue;

	private boolean isFlippedX;
	private boolean isFlippedY;
	private Boolean isFlippedIso;

	private MD5Key charsheetImage;
	private MD5Key portraitImage;

	private List<AttachedLightSource> lightSourceList;
	private String sightType;
	private boolean hasSight;
	private Boolean hasImageTable;
	private String imageTableName;

	private String label;

	/**
	 * The notes that are displayed for this token.
	 */
	private String notes;

	private String gmNotes;

	private String gmName;

	/**
	 * A state properties for this token. This allows state to be added that can change appearance of the token.
	 */
	private Map<String, Object> state;

	/**
	 * Properties
	 */
	// I screwed up.  propertyMap was HashMap<String,Object> in pre-1.3b70 (?)
	// and became a CaseInsensitiveHashMap<Object> thereafter.  So in order to
	// be able to load old tokens, we need to read in the original data type and
	// copy the elements into the new data type.  But because the name didn't
	// change (that was the screw up) we have special code in readResolve() to
	// help XStream move the data around.
	private Map<String, Object> propertyMap; // 1.3b77 and earlier
	private CaseInsensitiveHashMap<Object> propertyMapCI;

	private Map<String, String> macroMap;
	private Map<Integer, Object> macroPropertiesMap;

	private Map<String, String> speechMap;

	// Deprecated, here to allow deserialization
	@SuppressWarnings("unused")
	private transient int size; // 1.3b16

	@SuppressWarnings("unused")
	private transient List<Object> visionList; // 1.3b18

	public enum ChangeEvent {
		name, MACRO_CHANGED
	}

	public AndroidToken asAndroidToken() {
		return new AndroidToken(id, beingImpersonated, exposedAreaGUID, x, y, z, anchorX, anchorY, lastX, lastY, name, ownerList);
	}
	
}
