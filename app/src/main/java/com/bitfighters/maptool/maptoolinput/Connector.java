package com.bitfighters.maptool.maptoolinput;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.CheckBox;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import com.bitfighters.maptool.maptoolinput.clientserver.simple.AbstractConnection;
import com.bitfighters.maptool.maptoolinput.clientserver.simple.DisconnectHandler;
import com.bitfighters.maptool.maptoolinput.implClient.ClientMethodHandler;
import com.bitfighters.maptool.maptoolinput.implClient.MapToolConnection;

import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.ZonePoint;

public class Connector{

	private String ip, username, password;
	private int port;
	//private MainActivity activity;
	private boolean isConnected;
	public static PendingIntent alarmIntent;
	public static Connector currentConnection;
	public MapToolConnection conn;

	private Activity activity;

	public void setActivity(Activity context) {
		this.activity = context;
	}

	public Connector(String ip, int port, String username, String password) {
		this.ip = ip;
		this.username = username;
		this.port = port;
		this.password = password;
	}


	public void registerAlarm(Context context) {
		Intent i = new Intent(context, AlarmReceiver.class);

		alarmIntent = PendingIntent.getBroadcast(context,0, i, 0);

		// We want the alarm to go off 3 seconds from now.
		long firstTime = SystemClock.elapsedRealtime();
		firstTime += 3 * 1000;//start 3 seconds after first register.

		// Schedule the alarm!
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
				60000, alarmIntent);//1min interval

	}

	public void doDisconnect() {
		try {
			conn.close();
		} catch (IOException e) {
		}
		disconnect();
	}

	public void toggleWaypoint(){

		AndroidToken myToken = MyData.instance.currentToken;
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentZone + ".");
		}else{
			Position pos = MyData.instance.currentTokenMovePosition;
			if(pos == null){
			}else{
				int x = pos.x ;
				int y = pos.y;
				callMethod("toggleTokenMoveWaypoint", MyData.instance.currentZone.id, myToken.id, new ZonePoint(x,y));
			}
		}

	}

	public void move() {
		if(MyData.instance.currentTokenMovePosition == null)
			return;

		AndroidToken myToken = MyData.instance.currentToken;
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentZone + ".");
		}else{
			callMethod("commitMoveSelectionSet", MyData.instance.currentZone.id, myToken.id);
		}
		MyData.instance.currentTokenMovePosition = null;
	}

	public void moveUp() {
		checkMoving();

		Position pos = MyData.instance.moveTop();
		if(pos != null)
			callMethod("updateTokenMove", MyData.instance.currentZone.id, MyData.instance.currentToken.id, pos.x, pos.y);
	}

	public void moveRight() {
		checkMoving();

		Position pos = MyData.instance.moveRight();
		if(pos != null)
			callMethod("updateTokenMove", MyData.instance.currentZone.id, MyData.instance.currentToken.id, pos.x, pos.y);
	}

	public void moveDown() {
		checkMoving();

		Position pos = MyData.instance.moveBottom();
		if(pos != null)
			callMethod("updateTokenMove", MyData.instance.currentZone.id, MyData.instance.currentToken.id, pos.x, pos.y);
	}

	public void moveLeft() {
		checkMoving();

		Position pos = MyData.instance.moveLeft();
		if(pos != null)
			callMethod("updateTokenMove", MyData.instance.currentZone.id, MyData.instance.currentToken.id, pos.x, pos.y);
	}

	private void checkMoving() {
		if(MyData.instance.currentTokenMovePosition == null){
			AndroidToken myToken = MyData.instance.currentToken;
			if(myToken == null){
				//showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
			}else{
				MyData.instance.startMove();

				LinkedHashSet<GUID> set = new LinkedHashSet<GUID>();
				set.add(myToken.id);

				callMethod("startTokenMove", conn.player.getName(), MyData.instance.currentZone.id, myToken.id, set);
			}
		}
	}

	public void rollDice(int dice) {
		int maxDice = dice;
		int roll = (int)(Math.random()*dice)+1;
		LinkedList<String> str = new LinkedList<String>();
		String me = conn.player.getName();
		String message = "* " + me + " würfelt: d"+dice+" => " + roll;
		callMethod("message", TextMessage.all(str,me, message));

		showInfo( "Result: " + roll, "Rolled d"+dice);
	}

	private class asyncConnectionTask extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... urls) {
			doConnect();
			return 0L;
		}

		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Long result) {
			//showDialog("Downloaded " + result + " bytes");
		}
	}

	private void callMethod(String method, Object... parameters) {
		new SendMethodTask(method).execute(parameters);
	}

	private class SendMethodTask extends AsyncTask<Object, Integer, Long> {
		private String method;
		public SendMethodTask(String method){
			super();
			this.method = method;
		}

		protected Long doInBackground(Object... data) {
			conn.callMethod(method, data);
			return 0L;
		}

		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Long result) {
			//showDialog("Downloaded " + result + " bytes");
		}
	}

	public void connect(){
		new asyncConnectionTask().execute();
	}

	public void doConnect(){
		try {
			conn = new MapToolConnection(ip, port, new Player(username, Role.PLAYER, password));
			ClientMethodHandler handler = new ClientMethodHandler();

			conn.addMessageHandler(handler);
			//conn.addActivityListener(clientFrame.getActivityMonitor());
			conn.addDisconnectHandler(new DisconnectHandler() {

				@Override
				public void handleDisconnect(AbstractConnection conn) {
					System.err.println("Disconnect");
					disconnect();
				}
			});


			conn.start();
			currentConnection = this;
			//registerAlarm(activity);

		} catch (IOException e) {
			if(LoginActivity.userLoginTaskInstance != null){
				LoginActivity.userLoginTaskInstance.setConnectionError(e.getMessage());
			}
			showAlert(e.getMessage());
		}
	}


	public void showAlert(String message) {
		System.out.println("[ALERT]" + message);
		if(activity != null)
			new AlertDialog.Builder(activity)
					.setTitle("Error")
					.setMessage(message)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// continue with delete
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
	}

	public void showInfo(String message, String title) {
		System.out.println("[Info]" + message);
		if(activity != null)
			new AlertDialog.Builder(activity)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// continue with delete
					}
				})
				.setIcon(android.R.drawable.ic_dialog_info)
				.show();
	}

	void sendHeartbeat() {
		if(conn != null){
			callMethod("heartbeat", conn.player.getName());
		}
	}

	protected void disconnect() {
		//Handle being disconnected
		if(currentConnection == this)
			currentConnection = null;

		AlarmManager alarmMgr = (AlarmManager) activity
				.getSystemService(Context.ALARM_SERVICE);
		if (alarmMgr!= null) {
			alarmMgr.cancel(alarmIntent);
		}
	}

	public void CheckData(){
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				CheckBox mapLoaded = (CheckBox) activity.findViewById(R.id.mapLoaded);
				CheckBox charLoaded = (CheckBox) activity.findViewById(R.id.characterLoaded);
				CheckBox allCharLoadede = (CheckBox) activity.findViewById(R.id.allCharacterLoaded);

				boolean isMapLoaded = MyData.instance.currentZone != null;
				if (!isMapLoaded) {
					mapLoaded.setChecked(false);
					charLoaded.setChecked(false);
					allCharLoadede.setChecked(false);
				} else {
					mapLoaded.setChecked(true);

					AndroidToken myToken = MyData.instance.currentToken;
					if (myToken == null) {
						charLoaded.setChecked(false);
						allCharLoadede.setChecked(false);
					} else if (myToken.name.trim().equals("ALL")) {
						charLoaded.setChecked(false);
						allCharLoadede.setChecked(true);
					} else {
						charLoaded.setChecked(true);
						allCharLoadede.setChecked(false);
					}
				}
				mapLoaded.invalidate();
				charLoaded.invalidate();
				allCharLoadede.invalidate();
			}
		});
	}
}
