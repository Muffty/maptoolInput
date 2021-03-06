package com.bitfighters.maptool.maptoolinput.implClient;

import java.io.IOException;
import java.net.Socket;

import com.bitfighters.maptool.maptoolinput.LoginActivity;
import com.bitfighters.maptool.maptoolinput.clientserver.hessian.client.ClientConnection;

import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool.server.Handshake;

public class MapToolConnection extends ClientConnection {
	public final Player player;
	private String mapToolVersion = "1.4.0.5";

	public MapToolConnection(String host, int port, Player player) throws IOException {
		super(host, port, null);
		this.player = player;
	}

	public MapToolConnection(Socket socket, Player player) throws IOException {
		super(socket, null);
		this.player = player;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rptools.clientserver.simple.client.ClientConnection#sendHandshake(java.net.Socket)
	 */
	@Override
	public boolean sendHandshake(Socket s) throws IOException {
		Handshake.Response response = Handshake.sendHandshake(new Handshake.Request(player.getName(), player.getPassword(), Role.PLAYER, mapToolVersion), s);

		if (response.code != Handshake.Code.OK) {
			LoginActivity.userLoginTaskInstance.setConnectionError(response.message);
			return false;
		}else{
			LoginActivity.userLoginTaskInstance.connectionDone();
			return true;
		}
	}
}