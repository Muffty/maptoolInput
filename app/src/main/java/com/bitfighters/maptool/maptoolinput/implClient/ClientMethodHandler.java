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

import com.bitfighters.maptool.maptoolinput.Connector;
import com.bitfighters.maptool.maptoolinput.MyData;
import com.bitfighters.maptool.maptoolinput.clientserver.hessian.AbstractMethodHandler;
import com.bitfighters.maptool.maptoolinput.implClient.ClientCommand.COMMAND;

import net.rptools.maptool.model.AndroidCampaign;
import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;

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

        }else if(cmd == COMMAND.updateTokenMove){

            GUID map = (GUID)parameters[0];
            GUID token = (GUID)parameters[1];
            int x = (int)parameters[2];
            int y = (int)parameters[3];

            MyData.instance.handleUpdateTokenMove(map, token, x, y);

        }else if(cmd == COMMAND.enforceZone){
            MyData.instance.setCurrentZone((GUID)parameters[0]);
        }else if(cmd == COMMAND.androidSetCampaign){
			MyData.instance.initiateCampaign((AndroidCampaign)parameters[0]);
		}
	}
}
