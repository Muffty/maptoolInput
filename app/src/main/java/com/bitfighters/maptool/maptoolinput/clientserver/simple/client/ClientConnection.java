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
package com.bitfighters.maptool.maptoolinput.clientserver.simple.client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.bitfighters.maptool.maptoolinput.clientserver.simple.AbstractConnection;

/**
 * @author drice
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class ClientConnection extends AbstractConnection {
	private final Socket socket;
	private SendThread send;
	private ReceiveThread receive;
	private final String id;

	public ClientConnection(String host, int port, String id) throws UnknownHostException, IOException {
		this(new Socket(host, port), id);
	}

	public ClientConnection(Socket socket, String id) {
		this.socket = socket;
		this.id = id;
	}

	public void start() throws IOException {
		if (sendHandshake(socket)) {
			this.send = new SendThread(this, socket.getOutputStream());
			this.send.start();
			this.receive = new ReceiveThread(this, socket.getInputStream());
			this.receive.start();
		} else {
			socket.close();
		}
	}

	public boolean sendHandshake(Socket s) throws IOException {
		return true;
	}

	public String getId() {
		return id;
	}

	public void sendMessage(byte[] message) {
		sendMessage(null, message);
	}

	public void sendMessage(Object channel, byte[] message) {
		addMessage(channel, message);
		synchronized (send) {
			send.notify();
		}
	}

	public boolean isAlive() {
		return !socket.isClosed();
	}

	public synchronized void close() throws IOException {
		if (send.stopRequested) {
			return;
		}
		socket.close();
		send.requestStop();
		receive.requestStop();

		//        try {
		//            send.join();
		//            // receive.join(); <-- This causes a freaky error.  whatever.
		//        } catch (InterruptedException e) {
		//            e.printStackTrace();
		//        }
	}

	// /////////////////////////////////////////////////////////////////////////
	// send thread
	// /////////////////////////////////////////////////////////////////////////
	private class SendThread extends Thread {
		private final ClientConnection conn;
		private final OutputStream out;
		private boolean stopRequested = false;

		public SendThread(ClientConnection conn, OutputStream out) {
			this.conn = conn;
			this.out = new BufferedOutputStream(out, 1024);
		}

		public void requestStop() {
			this.stopRequested = true;
			synchronized (this) {
				this.notify();
			}
		}

		@Override
		public void run() {
			try {
				while (!stopRequested && conn.isAlive()) {
					try {
						while (conn.hasMoreMessages()) {
							try {
								byte[] message = conn.nextMessage();
								if (message == null) {
									continue;
								}
								conn.writeMessage(out, message);
							} catch (IndexOutOfBoundsException e) {
								// just ignore and wait
							}
						}
						synchronized (this) {
							if (!stopRequested) {
								this.wait();
							}
						}
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			} catch (IOException e) {
				fireDisconnect();
			}
		}

	}

	// /////////////////////////////////////////////////////////////////////////
	// receive thread
	// /////////////////////////////////////////////////////////////////////////
	private class ReceiveThread extends Thread {
		private final ClientConnection conn;
		private final InputStream in;
		private boolean stopRequested = false;

		public ReceiveThread(ClientConnection conn, InputStream in) {
			this.conn = conn;
			this.in = in;
		}

		public void requestStop() {
			stopRequested = true;
		}

		@Override
		public void run() {
			while (!stopRequested && conn.isAlive()) {
				try {
					byte[] message = conn.readMessage(in);
					conn.dispatchMessage(conn.id, message);
				} catch (IOException e) {
					fireDisconnect();
					break;
				} catch (Throwable t) {
					// don't let anything kill this thread via exception
					t.printStackTrace();
				}
			}
		}
	}
}
