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

package com.bitfighters.maptool.maptoolinput.implClient;

import android.app.ActivityManager;

import com.bitfighters.maptool.maptoolinput.Connector;
import com.bitfighters.maptool.maptoolinput.LoginActivity;
import com.bitfighters.maptool.maptoolinput.MainTab;
import com.bitfighters.maptool.maptoolinput.MyData;
import com.bitfighters.maptool.maptoolinput.clientserver.hessian.AbstractMethodHandler;
import com.bitfighters.maptool.maptoolinput.implClient.ClientCommand.COMMAND;

import net.rptools.maptool.transfer.AssetChunk;
import net.rptools.maptool.transfer.AssetConsumer;
import net.rptools.maptool.transfer.AssetHeader;
import net.rptools.maptool.transfer.AssetTransferManager;

import net.rptools.maptool.model.AndroidCampaign;
import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;

import java.io.IOException;
import java.util.List;

public class ClientMethodHandler extends AbstractMethodHandler {
	public ClientMethodHandler() {
	}

	public void handleMethod(final String id, final String method, final Object... parameters) {

		System.out.println("handleMethod[ID"+id+"]:" + method + " with " + parameters.length + " parameters:");
		for (int i = 0; i < parameters.length; i++) {
			if(parameters[i] == null)
				System.out.print("\t ["+i+"]null");
			else
				System.out.print("\t ["+i+":"+parameters[i].getClass().toString()+"]"+parameters[i]);
		}
		System.out.println();
				
		final ClientCommand.COMMAND cmd = Enum.valueOf(ClientCommand.COMMAND.class, method);

		if(cmd == COMMAND.androidPutToken){
            GUID map = (GUID)parameters[0];
            AndroidToken token = (AndroidToken)parameters[1];

			MyData.instance.handlePutToken(map, token);

            if(Connector.currentConnection != null)
                Connector.currentConnection.CheckData();
			if(MainTab.instance != null)
				MainTab.instance.sendUpdateView();

        }else if(cmd == COMMAND.updateTokenMove){

            GUID map = (GUID)parameters[0];
            GUID token = (GUID)parameters[1];
            int x = (int)parameters[2];
            int y = (int)parameters[3];

            MyData.instance.handleUpdateTokenMove(map, token, x, y);
			//if(MainTab.instance != null)
			//	MainTab.instance.sendUpdateView();

        }else if(cmd == COMMAND.enforceZone){
            MyData.instance.setCurrentZone((GUID)parameters[0]);
			if(MainTab.instance != null)
				MainTab.instance.sendUpdateView();
        }else if(cmd == COMMAND.androidSetCampaign){
			MyData.instance.initiateCampaign((AndroidCampaign)parameters[0]);
			if(MainTab.instance != null)
				MainTab.instance.sendUpdateView();
		}else if(cmd == COMMAND.putAsset){
			MyData.instance.putAsset((Asset)parameters[0]);
			if(MainTab.instance != null)
				MainTab.instance.sendUpdateView();
		}else if(cmd == COMMAND.startAssetTransfer){
			AssetHeader header = (AssetHeader) parameters[0];
			if(MainTab.instance == null)
				AssetTransferManager.getInstance().addConsumer(new AssetConsumer(LoginActivity.instance.getCacheDir(), header));
			else
				AssetTransferManager.getInstance().addConsumer(new AssetConsumer(MainTab.instance.getCacheDir(), header));
			return;
		}else if(cmd == COMMAND.updateAssetTransfer){
			AssetChunk chunk = (AssetChunk) parameters[0];
			try {
				AssetTransferManager.getInstance().update(chunk);
			} catch (IOException ioe) {
				// TODO: do something intelligent like clear the transfer manager, and clear the "we're waiting for" flag so that it gets requested again
				ioe.printStackTrace();
			}
			return;
		}else if(cmd == COMMAND.androidVision){
			MyData.instance.setVision((GUID)parameters[0], (List<GUID>) parameters[1]);
            if(MainTab.instance != null)
                MainTab.instance.sendUpdateView();
		}else if(cmd == COMMAND.message){
			String receiver = ((String)parameters[4]).trim();
			if(receiver.equals(Connector.currentConnection.getUsername().trim()))
				MainTab.instance.handleMessage((String)parameters[6], (String)parameters[8]);
		}
	}

}
