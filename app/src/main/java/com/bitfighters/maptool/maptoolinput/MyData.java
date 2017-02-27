package com.bitfighters.maptool.maptoolinput;

import java.util.HashMap;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;

public class MyData {

	public static MyData instance;
	
	public HashMap<GUID, HashMap<GUID, Token>> tokensMaps;
	public HashMap<GUID, HashMap<GUID, Position>> tokenPositionMaps;
	public GUID currentMap;
	
	public MyData(){
		instance = this;
		tokensMaps = new HashMap<>();
		tokenPositionMaps = new HashMap<>();
	}
	
	public void updateTokenPosition(Token token, int x, int y){

		if(currentMap == null){
			Connector.currentConnection.showAlert("Aktuelle Map nicht bekannt (Strg+E auf Server)!");
			return;
		}
		HashMap<GUID, Position> tokenPosition;
		if(!tokenPositionMaps.containsKey(currentMap)){
			tokenPosition = new HashMap<>();
			tokenPositionMaps.put(currentMap,tokenPosition);
		}else{
			tokenPosition = tokenPositionMaps.get(currentMap);
		}

		if(tokenPosition.containsKey(token.id)){
			tokenPosition.get(token.id).update(x,y);
		}else{
			tokenPosition.put(token.id, new Position(x, y));
		}
	}
	
	public Position GetTokenPosition(GUID id){

		if(currentMap == null){
			Connector.currentConnection.showAlert("Aktuelle Map nicht bekannt (Strg+E auf Server)!");
			return null;
		}
		HashMap<GUID, Position> tokenPosition;
		if(!tokenPositionMaps.containsKey(currentMap)){
			tokenPosition = new HashMap<>();
			tokenPositionMaps.put(currentMap,tokenPosition);
		}else{
			tokenPosition = tokenPositionMaps.get(currentMap);
		}

		HashMap<GUID, Token> tokens;
		if(!tokensMaps.containsKey(currentMap)){
			tokens = new HashMap<>();
			tokensMaps.put(currentMap,tokens);
		}else{
			tokens = tokensMaps.get(currentMap);
		}

		if(tokenPosition.containsKey(id)){
			return tokenPosition.get(id);
		}else if(tokens.containsKey(id)){
			return new Position(tokens.get(id).lastX,tokens.get(id).lastY);
		}else{
			return null;
		}
	}
	
	public Token getPcTokenByName(String name){

		HashMap<GUID, Token> tokens;

		if(!tokensMaps.containsKey(currentMap)){
			return null;
		}else{
			tokens = tokensMaps.get(currentMap);
		}

        boolean foundNonPc = false;
        Token allToken = null;

		for (Token token : tokens.values()) {
			if(token.name.trim().equals(name.trim()))
                if(token.ownerType == Token.Type.PC.ordinal())
				    return token;
                else
                    foundNonPc = true;
            else if(token.name.trim().equals("ALL") && token.ownerType == Token.Type.PC.ordinal())
                allToken = token;
		}

        if(allToken == null && foundNonPc){
            Connector.currentConnection.showAlert("No PC '"+name+"' found but an NPC!");
        }

        return allToken;
	}

	public boolean hasToken(GUID map, Token token) {

		if(!tokensMaps.containsKey(map)){
			return false;
		}else{
			HashMap<GUID, Token> tokens = tokensMaps.get(currentMap);
			return tokens.containsKey(token.id);
		}

	}

	public void addToken(GUID map, Token token, int x, int y) {
		HashMap<GUID, Token> tokens;
		if(!tokensMaps.containsKey(map)){
			tokens = new HashMap<>();
			tokensMaps.put(map, tokens);
			tokens.put(token.id, token);
		}else{
			tokens = tokensMaps.get(map);
			tokens.put(token.id, token);
		}

		HashMap<GUID, Position> tokenPositions;
		if(!tokenPositionMaps.containsKey(map)){
			tokenPositions = new HashMap<>();
			tokenPositionMaps.put(map, tokenPositions);
			tokenPositions.put(token.id, new Position(x,y));
		}else{
			tokenPositions = tokenPositionMaps.get(map);
			tokenPositions.put(token.id,  new Position(x,y));
		}
	}

	public void updateTokenByID(GUID map, GUID tokenID, int x, int y) {


		HashMap<GUID, Position> tokenPositions;
		if(!tokenPositionMaps.containsKey(map)){
			tokenPositions = new HashMap<>();
			tokenPositionMaps.put(map, tokenPositions);
			tokenPositions.put(tokenID, new Position(x,y));
		}else{
			tokenPositions = tokenPositionMaps.get(map);
			if(tokenPositions.containsKey(tokenID)){
				tokenPositions.get(tokenID).update(x,y);
			}else
				tokenPositions.put(tokenID,  new Position(x,y));
		}
	}

    public void updateToken(GUID map, Token token) {
        HashMap<GUID, Token> tokens;
        if(!tokensMaps.containsKey(map)){
            tokens = new HashMap<>();
            tokensMaps.put(map, tokens);
            tokens.put(token.id, token);
        }else{
            tokens = tokensMaps.get(map);
            tokens.remove(token.id);
            tokens.put(token.id, token);
        }
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
