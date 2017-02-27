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

package net.rptools.maptool.server;

import java.io.IOException;
import java.net.Socket;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

//import com.bitfighters.maptool.maptoolinput.caucho.hessian.io.HessianInput;
//import com.bitfighters.maptool.maptoolinput.caucho.hessian.io.HessianOutput;

import net.rptools.maptool.model.Player;

/**
 * @author trevor
 */
public class Handshake {

	public interface Code {
		public static final int UNKNOWN = 0;
		public static final int OK = 1;
		public static final int ERROR = 2;
	}

	/**
	 * Client side of the handshake
	 */
	public static Response sendHandshake(Request request, Socket s) throws IOException {
		HessianInput input = new HessianInput(s.getInputStream());
		HessianOutput output = new HessianOutput(s.getOutputStream());
		output.findSerializerFactory().setAllowNonSerializable(true);
		output.writeObject(request);

		return (Response) input.readObject();
	}

	public static class Request {
		public String name;
		public String password;
		public String role;
		public String version;

		public Request() {
			// for serialization
		}

		public Request(String name, String password, Player.Role role, String version) {
			this.name = name;
			this.password = password;
			this.role = role.name();
			this.version = version;
		}
	}

	public static class Response {
		public int code;
		public String message;
		public ServerPolicy policy;
	}
}
