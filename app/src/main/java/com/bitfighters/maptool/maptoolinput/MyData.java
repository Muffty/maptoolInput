package com.bitfighters.maptool.maptoolinput;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.AndroidCampaign;
import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.AndroidZone;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static android.R.attr.path;

public class MyData {

	public static MyData instance;

	public AndroidCampaign campaign;

	public AndroidZone currentZone;
	public AndroidToken currentToken;
	public Position currentTokenMovePosition;

	private String myTokenName;

	public HashMap<MD5Key, Bitmap> bitmaps;

	private HashSet<MD5Key> bitmapsLoading;

	private HashMap<GUID, List<GUID>> visionMaps;

	private LocalFileReader fileReader;

	public MyData(String myTokenName){
		instance = this;
		this.myTokenName = myTokenName;
		bitmaps = new HashMap<MD5Key, Bitmap>();
		bitmapsLoading = new HashSet<>();
        visionMaps = new HashMap<>();
		fileReader = new LocalFileReader();
	}

	public void initiateCampaign(AndroidCampaign campaign){
		this.campaign = campaign;

		for (AndroidZone zone:campaign.zones.values()) {
			for (AndroidToken token:zone.tokenMap.values()) {
				token.LoadImages();
			}
		}

	}

	public void setCurrentZone(GUID zoneId){
		if(currentZone != null && currentZone.id == zoneId)
			return;

		if(campaign == null){
			alert("No campaign loaded!");
			return;
		}
		currentZone = campaign.zones.get(zoneId);

		if(currentZone == null){
			alert("Unknown Zone: "+zoneId);
			return;
		}

		currentTokenMovePosition = null;
		updateMyToken();
	}

	public void handlePutToken(GUID zoneId, AndroidToken token) {
		AndroidZone zone = campaign.zones.get(zoneId);
		if(zone == null){
			alert("Unknown Zone: "+zoneId);
			return;
		}

		zone.tokenMap.put(token.id, token);
		updateMyToken();

		token.LoadImages();
	}


	public void handleUpdateTokenMove(GUID mapId, GUID tokenId, int x, int y) {
		if(currentToken != null && tokenId == currentToken.id){
			currentTokenMovePosition = new Position(x,y);
		}
	}

	private void updateMyToken() {
		if(currentToken != null && currentZone.tokenMap.containsKey(currentToken.id)){
			currentToken = currentZone.tokenMap.get(currentToken.id);

			//check for currentToken name change to invalid myToken:
			if(!currentToken.name.trim().equals(myTokenName.trim()) && !currentToken.name.trim().equals("ALL")){
				currentToken = null;
				updateMyToken();
			}

		}else{
			//Find my Token in currentMap

			boolean foundNonPc = false;
			AndroidToken allToken = null;

			for (AndroidToken token: currentZone.tokenMap.values()) {
				if (token.name.trim().equals(myTokenName.trim())) {
					currentToken = token;
					return;
				} else if (token.name.trim().equals("ALL"))
					allToken = token;
			}

			currentToken = allToken;	//<- Maybe == null, but okay
		}

		if(currentToken == null)
			currentTokenMovePosition = null;
	}

	public void alert(String message){
		Connector.currentConnection.showAlert(message);
	}

	public Position moveRight(){
		return move(1,0);
	}

	public Position moveLeft(){
		return move(-1,0);
	}

	public Position moveTop(){
		return move(0,-1);
	}

	public Position moveBottom(){
		return move(0,1);
	}

	public Position moveTo(int x, int y) {
		if(currentToken == null || currentZone == null)
			return null;
		else{

			if(currentTokenMovePosition == null){
				currentTokenMovePosition = new Position(x, y);
				return currentTokenMovePosition;
			}else{
				currentTokenMovePosition.update(x, y);
				return currentTokenMovePosition;
			}
		}
	}

	private Position move(int xMove, int yMove) {
		if(currentToken == null || currentZone == null)
			return null;
		else{

			int xOffset = currentZone.gridSize * xMove;
			int yOffset = currentZone.gridSize * yMove;

			if(currentTokenMovePosition == null){
				currentTokenMovePosition = new Position(currentToken.x + xOffset, currentToken.y + yOffset);
				return currentTokenMovePosition;
			}else{
				currentTokenMovePosition.update(currentTokenMovePosition.x + xOffset, currentTokenMovePosition.y + yOffset);
				return currentTokenMovePosition;
			}
		}
	}

	public void startMove() {
		if(currentToken != null && currentTokenMovePosition == null){
			currentTokenMovePosition = new Position(currentToken.x, currentToken.y);
		}
	}

	public Bitmap getBitmap(MD5Key key) {
		Bitmap bitmap = bitmaps.get(key);
		if(bitmap == null)
			bitmap = loadLocalCashBitmap(key);

		return bitmap;
	}

	public Bitmap loadLocalCashBitmap(MD5Key key){
		String file = key.toString();
		if(fileReader.hasFile(file)){
			Bitmap bmp = fileReader.readFileAsBitmap(file);
			bitmaps.put(key, bmp);
			return bmp;
		}
		return null;
	}
	public boolean haveBitmapCashed(MD5Key image) {
		return fileReader.hasFile(image.toString());
	}

	public Bitmap getBitmapOrDefault(MD5Key key, Bitmap defaultMap) {
		Bitmap map = getBitmap(key);
		if(map == null)
			map = defaultMap;
		return map;
	}

	public void putAsset(Asset asset){
		System.out.println("Put Asset" + asset.name);
		Bitmap map = BitmapFactory.decodeByteArray(asset.image, 0, asset.image.length);
		bitmaps.put(asset.id, map);
		fileReader.saveToInternalStorage(asset.id.toString(), map);
		bitmapsLoading.remove(asset.id);
	}

	public Collection<AndroidToken> getCurrentZoneCharacters() {
		if(currentZone == null)
			return new LinkedList<>();
		return currentZone.tokenMap.values();
	}

	public boolean loading(MD5Key image) {
		return bitmapsLoading.contains(image);
	}

	public void notifyLoad(MD5Key image) {
		bitmapsLoading.add(image);
	}

	public AndroidToken getToken(GUID tokenID) {
		for(AndroidZone zone: campaign.zones.values()){
			for (AndroidToken token: zone.tokenMap.values()) {
				if (token.id.equals(tokenID))
					return token;
			}
		}
		return null;
	}

	public boolean isTokenInVisibleArea(AndroidToken token) {
		List<GUID> vision = visionMaps.get(currentZone.id);
		return vision != null && vision.contains(token.id);
	}

	public void setVision(GUID map, List<GUID> visibleTokens){
		visionMaps.put(map, visibleTokens);
	}

}

class Position{
	public int x,y;

	public Position(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public void update(int x, int y) {
		this.x = x;
		this.y = y;
	}

}
class LocalFileReader{

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public boolean hasFile(String filename) {
		ContextWrapper cw = new ContextWrapper(MainTab.instance.getBaseContext());

		String path = cw.getDir("imageDir", Context.MODE_PRIVATE).getAbsolutePath();

		File f=new File(path, filename+".jpg");

		return f.exists();
	}

	public Bitmap readFileAsBitmap(String filename){
		ContextWrapper cw = new ContextWrapper(MainTab.instance.getBaseContext());

		String path = cw.getDir("imageDir", Context.MODE_PRIVATE).getAbsolutePath();

		try {
			File f=new File(path, filename+".jpg");
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
			return b;
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
	}

	public void saveToInternalStorage(String filename, Bitmap bitmapImage){
		ContextWrapper cw = new ContextWrapper(MainTab.instance.getBaseContext());
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
		// Create imageDir
		File mypath=new File(directory,filename+".jpg");

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(mypath);
			// Use the compress method on the BitMap object to write image to the OutputStream
			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}