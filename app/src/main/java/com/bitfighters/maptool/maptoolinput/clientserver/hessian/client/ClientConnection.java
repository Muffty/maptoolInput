/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.bitfighters.maptool.maptoolinput.clientserver.hessian.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.bitfighters.maptool.maptoolinput.clientserver.hessian.HessianUtils;

/**
 * @author drice
 */
public class ClientConnection extends com.bitfighters.maptool.maptoolinput.clientserver.simple.client.ClientConnection {

	public ClientConnection(String host, int port, String id) throws UnknownHostException, IOException {
		super(host, port, id);
	}

	public ClientConnection(Socket socket, String id) throws IOException {
		super(socket, id);
	}

	public void callMethod(String method, Object... parameters) {
		System.out.println("Client callMethod:" + method + " with " + parameters.length + " parameters ");
		if(method.equals("putToken"))
			return;
		byte[] message = HessianUtils.methodToBytesGZ(method, parameters);
		sendMessage(message);
	}
}
