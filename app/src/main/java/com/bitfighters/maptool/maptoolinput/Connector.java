package com.bitfighters.maptool.maptoolinput;

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

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.ZonePoint;

public class Connector{

	private String ip, username;
	private int port;
	private MainActivity context;
	private boolean isConnected;
	public static PendingIntent alarmIntent;
	public static Connector currentConnection;
	public MapToolConnection conn;
	private boolean moving;
	public static int gridSize = 50;

	public Connector(String ip, int port, String username, MainActivity context) {
		this.ip = ip;
		this.username = username;
		this.port = port;
		this.context = context;
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
		if(!moving)
			return;

		Token myToken = myToken();
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
		}else{
			Position pos = MyData.instance.GetTokenPosition(myToken.id);
			if(pos == null){
				showAlert("[Programming error] No position for token '"+myToken.name+"' found!");
			}else{
				int x = pos.x ;
				int y = pos.y;
				callMethod("toggleTokenMoveWaypoint", MyData.instance.currentMap, myToken.id, new ZonePoint(x,y));
			}
		}

	}

	public void move() {
		if(!moving)
			return;

		Token myToken = myToken();
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
		}else{
			callMethod("commitMoveSelectionSet", MyData.instance.currentMap, myToken.id);
			//callMethod("putToken", MyData.instance.currentMap, myToken);
		}
		moving = false;
	}

	public void moveUp() {
		checkMoving();
		Token myToken = myToken();
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
		}else{
			Position pos = MyData.instance.GetTokenPosition(myToken.id);
			if(pos == null){
				showAlert("[Programming error] No position for token '"+myToken.name+"' found!");
			}else{
				int x = pos.x + 0;
				int y = pos.y - gridSize;
				pos.update(x,y);
				callMethod("updateTokenMove", MyData.instance.currentMap, myToken.id, x, y);
			}
		}
	}

	public void moveRight() {
		checkMoving();
		Token myToken = myToken();
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
		}else{
			Position pos = MyData.instance.GetTokenPosition(myToken.id);
			if(pos == null){
				showAlert("[Programming error] No position for token '"+myToken.name+"' found!");
			}else{
				int x = pos.x + gridSize;
				int y = pos.y + 0;
				pos.update(x,y);
				callMethod("updateTokenMove", MyData.instance.currentMap, myToken.id, x, y);
			}
		}
	}

	public void moveDown() {
		checkMoving();
		Token myToken = myToken();
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
		}else{
			Position pos = MyData.instance.GetTokenPosition(myToken.id);
			if(pos == null){
				showAlert("[Programming error] No position for token '"+myToken.name+"' found!");
			}else{
				int x = pos.x + 0;
				int y = pos.y + gridSize;
				pos.update(x,y);
				callMethod("updateTokenMove", MyData.instance.currentMap, myToken.id, x, y);
			}
		}
	}

	public void moveLeft() {
		checkMoving();

		Token myToken = myToken();
		if(myToken == null){
			showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
		}else{
			Position pos = MyData.instance.GetTokenPosition(myToken.id);
			if(pos == null){
				showAlert("[Programming error] No position for token '"+myToken.name+"' found!");
			}else{
				int x = pos.x - gridSize;
				int y = pos.y + 0;
				pos.update(x,y);
				callMethod("updateTokenMove", MyData.instance.currentMap, myToken.id, x, y);
			}
		}
	}

	public Token myToken(){
		Token myToken = MyData.instance.getPcTokenByName(conn.player.getName());
		return myToken;
	}

	private void checkMoving() {
		if(!moving){
			Token myToken = myToken();
			if(myToken == null){
				//showAlert("No token '"+conn.player.getName()+"' found on map " + MyData.instance.currentMap + ".");
			}else{
				Position pos = MyData.instance.GetTokenPosition(myToken.id);
				if(pos == null){
					showAlert("[Programming error] No position for token '"+myToken.name+"' found!");
				}else{
					int x = pos.x + 0;
					int y = pos.y + 0;

					LinkedHashSet<GUID> set = new LinkedHashSet<GUID>();
					set.add(myToken.id);

					callMethod("startTokenMove", conn.player.getName(), MyData.instance.currentMap, myToken.id, set);
					moving = true;
				}
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

	private void doConnect(){
		try {
			conn = new MapToolConnection(ip, port, new Player(username, Role.PLAYER, "pw"));
			ClientMethodHandler handler = new ClientMethodHandler();

			conn.addMessageHandler(handler);
			//conn.addActivityListener(clientFrame.getActivityMonitor());
			conn.addDisconnectHandler(new DisconnectHandler() {

				@Override
				public void handleDisconnect(AbstractConnection conn) {
					disconnect();
				}
			});


			conn.start();
			currentConnection = this;
			registerAlarm(context);
		} catch (IOException e) {

			showAlert(e.getMessage());
		}
	}

	public void showAlert(String message) {
		new AlertDialog.Builder(context)
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
		new AlertDialog.Builder(context)
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

		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if (alarmMgr!= null) {
			alarmMgr.cancel(alarmIntent);
		}


		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				CheckBox mapLoaded = (CheckBox)context.findViewById(R.id.radioButton);
				CheckBox charLoaded = (CheckBox)context.findViewById(R.id.radioButton2);
				CheckBox allCharLoadede = (CheckBox)context.findViewById(R.id.radioButton3);

				mapLoaded.setChecked(false);
				charLoaded.setChecked(false);
				allCharLoadede.setChecked(false);

				mapLoaded.invalidate();
				charLoaded.invalidate();
				allCharLoadede.invalidate();

			}
		});
	}

	public void CheckData(){
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				CheckBox mapLoaded = (CheckBox) context.findViewById(R.id.radioButton);
				CheckBox charLoaded = (CheckBox) context.findViewById(R.id.radioButton2);
				CheckBox allCharLoadede = (CheckBox) context.findViewById(R.id.radioButton3);

				boolean isMapLoaded = MyData.instance.currentMap != null;
				if (!isMapLoaded) {
					mapLoaded.setChecked(false);
					charLoaded.setChecked(false);
					allCharLoadede.setChecked(false);
				} else {
					mapLoaded.setChecked(true);

					Token myToken = MyData.instance.getPcTokenByName(conn.player.getName());
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

	public void NotifyMapLoaded(GUID mapId){
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {


				CheckBox mapLoaded = (CheckBox) context.findViewById(R.id.radioButton);
				CheckBox charLoaded = (CheckBox) context.findViewById(R.id.radioButton2);
				CheckBox allCharLoadede = (CheckBox) context.findViewById(R.id.radioButton3);

				mapLoaded.setChecked(true);

				Token myToken = MyData.instance.getPcTokenByName(conn.player.getName());
				if (myToken == null)

				{
					charLoaded.setChecked(false);
					allCharLoadede.setChecked(false);
				} else if (myToken.name.trim() == "ALL")

				{
					charLoaded.setChecked(false);
					allCharLoadede.setChecked(true);
				} else

				{
					charLoaded.setChecked(true);
					allCharLoadede.setChecked(false);
				}

				mapLoaded.invalidate();
				charLoaded.invalidate();
				allCharLoadede.invalidate();
			}
		});
	}
}
