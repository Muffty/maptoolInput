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

import java.io.Serializable;

import com.withay.util.HexCode;


/**
 * Global unique identificator object.
 */
public class GUID extends Object implements Serializable, Comparable<GUID> {
	/** Serial version unique identifier. */
	private static final long serialVersionUID = 6361057925697403643L;

	/** GUIDs always have 16 bytes. */
	public static final int GUID_LENGTH = 16;

	// NOTE: THIS CAN NEVER BE CHANGED, OR IT WILL AFFECT ALL THINGS THAT PREVIOUSLY USED IT
	public static final int GUID_BUCKETS = 100;
	// NOTE: THIS CAN NEVER BE CHANGED, OR IT WILL AFFECT ALL THINGS THAT PREVIOUSLY USED IT

	private final byte[] baGUID;

	// Cache of the hashCode for a GUID
	private transient int hash;


	public GUID() {
		baGUID = null;
	}

	/** Creates a new GUID based on the specified GUID value. */
	public GUID(byte[] baGUID) {
		this.baGUID = baGUID;
		validateGUID();
	}

	/** Creates a new GUID based on the specified hexadecimal-code string. */
	public GUID(String strGUID) {

		this.baGUID = HexCode.decode(strGUID);
		validateGUID();
	}

	/** Ensures the GUID is legal. */
	private void validateGUID() {
	}

	/** Returns the GUID representation of the {@link byte} array argument. */
	public static GUID valueOf(byte[] bits) {
		if (bits == null)
			return null;
		return new GUID(bits);
	}

	/** Returns the GUID representation of the {@link String} argument. */
	public static GUID valueOf(String s) {
		if (s == null)
			return null;
		return new GUID(s);
	}

	/** Determines whether two GUIDs are equal. */
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return this == null;
		}
		Class<? extends Object> objClass = object.getClass();

		GUID guid;
		try {
			if (objClass == String.class) { // string
				guid = new GUID((String) object);
			} else { // try to cast to a GUID
				guid = (GUID) object;
			}
		} catch (ClassCastException e) { // not a GUID
			return false;
		}

		// Compare bytes.
		for (int i = 0; i < GUID_LENGTH; i++) {
			if (this.baGUID[i] != guid.baGUID[i])
				return false;
		}

		// All tests pass.
		return true;
	}

	public byte[] getBytes() {
		return baGUID;
	}

	/** Returns a string for the GUID. */
	@Override
	public String toString() {
		return HexCode.encode(baGUID, false); // false means uppercase
	}

	/**
	 * Returns a hashcode for this GUID. This function is based on the algorithm
	 * that JDK 1.3 uses for a String.
	 * 
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode() {
		int h = hash;
		if (h == 0) {
			byte val[] = baGUID;
			int len = GUID_LENGTH;

			for (int i = 0; i < len; i++)
				h = 31 * h + val[i];
			hash = h;
		}
		return h;
	}

	public int compareTo(GUID o) {
		if (o != this) {
			for (int i = 0; i < GUID_LENGTH; i++) {
				if (this.baGUID[i] != o.baGUID[i])
					return this.baGUID[i] - o.baGUID[i];
			}
		}
		return 0;
	}
}