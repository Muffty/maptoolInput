package com.bitfighters.maptool.maptoolinput;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telecom.Connection;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;

public class MainTab extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public static MainTab instance;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private int buttonRotation = 0;

    private ImageButton upButton, leftButton, downButton, rightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main_tab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Connector.currentConnection.setActivity(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_tab, menu);
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
            }else if (id == R.id.action_disconnect) {
                disconnect();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        MoveTabFragment tab1 = new MoveTabFragment();
        TabTwo tab2 = new TabTwo();
        TabThree tab3 = new TabThree();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return tab1;
            else if(position == 1)
                return tab2;
            return tab3;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }



    public static class MoveTabFragment extends Fragment {
        public MoveTabFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_movement, container, false);
            return rootView;
        }
    }

    public static class TabTwo extends Fragment {
        public TabTwo() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_tab_two, container, false);

            //TODO: initialise

            return rootView;
        }
    }


    public static class TabThree extends Fragment {

        public TabThree() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_tab_three, container, false);

            //TODO: initialise

            return rootView;
        }
    }


    public void updateGrid(View view){
        //Connector.gridSize = Integer.parseInt(((EditText)findViewById(R.id.gridSize)).getText().toString());
    }
    @Override
    public void onBackPressed() {
        disconnect();
    }
    public void disconnect(View view) {
        disconnect();
    }
    private void disconnect(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Disconnect")
                .setMessage("Are you sure you want to disconnect from server?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Connector.currentConnection != null){
                            Connector.currentConnection.doDisconnect();
                        }
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
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
            if(buttonRotation == 1)
                Connector.currentConnection.moveLeft();
            else if(buttonRotation == 2)
                Connector.currentConnection.moveDown();
            else if(buttonRotation == 3)
                Connector.currentConnection.moveRight();
            else if(buttonRotation == 0)
                Connector.currentConnection.moveUp();
        }
    }

    public void right(View view) {
        if(Connector.currentConnection != null){
            if(buttonRotation == 2)
                Connector.currentConnection.moveLeft();
            else if(buttonRotation == 3)
                Connector.currentConnection.moveDown();
            else if(buttonRotation == 0)
                Connector.currentConnection.moveRight();
            else if(buttonRotation == 1)
                Connector.currentConnection.moveUp();
        }
    }

    public void down(View view) {
        if(Connector.currentConnection != null){
            if(buttonRotation == 3)
                Connector.currentConnection.moveLeft();
            else if(buttonRotation == 0)
                Connector.currentConnection.moveDown();
            else if(buttonRotation == 1)
                Connector.currentConnection.moveRight();
            else if(buttonRotation == 2)
                Connector.currentConnection.moveUp();
        }
    }

    public void left(View view) {
        if(Connector.currentConnection != null){
            if(buttonRotation == 0)
                Connector.currentConnection.moveLeft();
            else if(buttonRotation == 1)
                Connector.currentConnection.moveDown();
            else if(buttonRotation == 2)
                Connector.currentConnection.moveRight();
            else if(buttonRotation == 3)
                Connector.currentConnection.moveUp();
        }
    }

    public void rotateRight(View view){
        buttonRotation = (buttonRotation+1)%4;
        updateButtonColors();
    }

    public void rotateLeft(View view){
        buttonRotation = (buttonRotation+3)%4;
        updateButtonColors();
    }

    private void updateButtonColors() {
        if(upButton == null){
            upButton= (ImageButton) findViewById(R.id.topButton);
            leftButton= (ImageButton) findViewById(R.id.leftButton);
            downButton= (ImageButton) findViewById(R.id.downButton);
            rightButton= (ImageButton) findViewById(R.id.rightButton);
        }

        upButton.setColorFilter(buttonRotation==0? Color.WHITE:Color.BLACK);
        leftButton.setColorFilter(buttonRotation==3? Color.WHITE:Color.BLACK);
        rightButton.setColorFilter(buttonRotation==1? Color.WHITE:Color.BLACK);
        downButton.setColorFilter(buttonRotation==2? Color.WHITE:Color.BLACK);
    }

}
