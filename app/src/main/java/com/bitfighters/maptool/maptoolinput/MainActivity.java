package com.bitfighters.maptool.maptoolinput;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Connector connector;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(Connector.currentConnection != null){

            if (id == R.id.action_d2) {
                Connector.currentConnection.rollDice(2);
                return true;
            }else if (id == R.id.action_d4) {
                Connector.currentConnection.rollDice(4);
                return true;
            }else if (id == R.id.action_d6) {
                Connector.currentConnection.rollDice(6);
                return true;
            }else if (id == R.id.action_d8) {
                Connector.currentConnection.rollDice(8);
                return true;
            }else if (id == R.id.action_d10) {
                Connector.currentConnection.rollDice(10);
                return true;
            }else if (id == R.id.action_d12) {
                Connector.currentConnection.rollDice(12);
                return true;
            }else if (id == R.id.action_d20) {
                Connector.currentConnection.rollDice(20);
                return true;
            }else if (id == R.id.action_d100) {
                Connector.currentConnection.rollDice(100);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void connect(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Whats your name?");

        // Set up the input
        final EditText input = new EditText(this);
        input.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("username", "User"));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onUsernameInput(input.getText().toString());
                //dialog.
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void updateGrid(View view){
        Connector.gridSize = Integer.parseInt(((EditText)findViewById(R.id.editText3)).getText().toString());
    }

    public void onUsernameInput(String username){

        MyData.instance = new MyData();
        String ip = "192.168.2.116";
        ip = ((EditText)findViewById(R.id.editText)).getText().toString();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("username", username).commit();
        ((TextView)findViewById(R.id.editText2)).setText(username);
        int port = 51234;
        connector = new Connector(ip, port, username, this);
        connector.connect();
    }

    public void disconnect(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.doDisconnect();
        }
    }

    public void waypoint(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.toggleWaypoint();
        }
    }

    public void walk(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.move();
        }
    }

    public void up(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.moveUp();
        }
    }

    public void right(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.moveRight();
        }
    }

    public void down(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.moveDown();
        }
    }

    public void left(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.moveLeft();
        }
    }


}