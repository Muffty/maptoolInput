package com.bitfighters.maptool.maptoolinput;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitfighters.maptool.maptoolinput.properties.PropertySettings;

import net.rptools.maptool.model.AndroidToken;
import net.rptools.maptool.model.GUID;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

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
    private long lastUpdate;

    private LinearLayout mCharacterList, mPropertyList;
    private ImageView mCharImage;
    private TextView mCharName;

    private int buttonRotation = 0;
    private boolean editMode;

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
            }else if (id == R.id.action_hidePointer) {
                Connector.currentConnection.hidePointer();
                return true;
            }else if (id == R.id.action_saveSettings) {
                PropertySettings.getInstance().saveSettings();
                return true;
            }else if (id == R.id.action_loadSettings) {
                PropertySettings.getInstance().loadSettings();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendUpdateView(){
        long now = System.currentTimeMillis();
        if(lastUpdate < now - 2000){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView();
                }
            });
        }

    }

    private void updateView() {
        //Updates View by data in MyData
        long now = System.currentTimeMillis();
        lastUpdate = now;

        updateCharacterList();
        updateLoadingIndicators();
        updateMyCharacterView();
    }

    private void updateMyCharacterView() {

        if(mPropertyList == null)
            return;

        //Clear current View:
        mPropertyList.removeAllViewsInLayout();
        if(MyData.instance.currentToken != null){
            mCharName.setText(MyData.instance.currentToken.name);

            Bitmap bitmap = MyData.instance.getBitmap(MyData.instance.currentToken.imageAssetMap.get(null));
            if(bitmap != null){
                mCharImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 80, 80, false));
            }else{
                mCharImage.setImageResource(R.mipmap.ic_launcher);
            }
            PropertySettings.getInstance().addAllViewsIn(mPropertyList, this, MyData.instance.currentToken,editMode,MyData.instance.currentZone.id);
        }



    }

    private void updateLoadingIndicators() {
        CheckBox mapLoaded = (CheckBox) findViewById(R.id.mapLoaded);
        CheckBox charLoaded = (CheckBox) findViewById(R.id.characterLoaded);
        CheckBox allCharLoadede = (CheckBox) findViewById(R.id.allCharacterLoaded);

        if(mapLoaded == null || charLoaded == null || allCharLoadede == null)
            return;

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

    private void updateCharacterList() {
        if(mCharacterList == null)
            return;

        //Clear current list:
        mCharacterList.removeAllViewsInLayout();

        //Generate PC list:
        fillCharacterList(true);

        //Generate NPC list:
        fillCharacterList(false);

    }

    private void fillCharacterList(boolean pc) {

        if(pc){

            View child = getLayoutInflater().inflate(R.layout.char_pcs, null);
            mCharacterList.addView(child);
        }else{

            View child = getLayoutInflater().inflate(R.layout.char_npcs, null);
            mCharacterList.addView(child);
        }

        Collection<AndroidToken> tokens = MyData.instance.getCurrentZoneCharacters();
        LinkedList<AndroidToken> showToken = new LinkedList<>();
        for (AndroidToken token: tokens) {
            if(token.pc == pc && token.isVisible && token.layer.equals("TOKEN") && MyData.instance.isTokenInVisibleArea(token)){
                showToken.add(token);
            }
        }
        final AndroidToken myToken = MyData.instance.currentToken;
        if(myToken != null){
            Collections.sort(showToken, new Comparator<AndroidToken>() {
                @Override
                public int compare(AndroidToken t1, AndroidToken t2) {
                    return (int)Math.signum(Math.max(Math.abs(t1.x - myToken.x),  Math.abs(t1.y - myToken.y)) - (Math.max(Math.abs(t2.x - myToken.x),  Math.abs(t2.y - myToken.y))));
                }
            });
        }

        for (int i = 0; i < showToken.size(); i++){
            final AndroidToken token = showToken.get(i);


            View child = getLayoutInflater().inflate(R.layout.char_entry, null);
            //Background color:
            if(mCharacterList.getChildCount()%2==(pc?0:1))
                child.setBackgroundColor(getResources().getColor(R.color.colorListEntry1));
            else
                child.setBackgroundColor(getResources().getColor(R.color.colorListEntry2));

            //Icon:
            Bitmap bitmap = MyData.instance.getBitmap(token.imageAssetMap.get(null));
            ImageButton imgButton = ((ImageButton)child.findViewById(R.id.charImage));
            if(bitmap != null){
                imgButton.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 80, 80, false));
            }else{
                imgButton.setImageResource(R.mipmap.ic_launcher);
            }

            imgButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            Connector.currentConnection.pointAt(token.x + MyData.instance.currentZone.gridSize/2, token.y + MyData.instance.currentZone.gridSize/2);
                            break;
                        case MotionEvent.ACTION_UP:
                            Connector.currentConnection.hidePointer();
                            break;
                        case MotionEvent.ACTION_BUTTON_RELEASE:
                            Connector.currentConnection.hidePointer();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            Connector.currentConnection.hidePointer();
                            break;
                        case MotionEvent.ACTION_POINTER_UP:
                            Connector.currentConnection.hidePointer();
                            break;
                        case MotionEvent.ACTION_SCROLL:
                            Connector.currentConnection.hidePointer();
                            break;
                    }
                    return true;
                }
            });

            mCharacterList.addView(child);




            //Distance:
            Button featDistance = (Button)child.findViewById(R.id.feetDistance);
            if(myToken != null){
                int distance = Math.max(Math.abs(myToken.x - token.x),Math.abs(myToken.y - token.y));
                distance /= MyData.instance.currentZone.gridSize/5;

                if(distance > 0)
                    distance -= 5;

                featDistance.setText(distance + " ft.");
            }else{
                featDistance.setText("?? ft.");
            }
            featDistance.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Connector.currentConnection.moveTo(token);
                }
            });

            //Name:
            Button cName = (Button)child.findViewById(R.id.cName);
            cName.setText(token.name);
            cName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showTokenInfo(token.id);
                }
            });
        }
    }

    private void showTokenInfo(GUID tokenID) {

        Intent intent = new Intent(getBaseContext(), CharacterDetail.class);
        startActivity(intent);
        CharacterDetail.characterToDisplay = tokenID;
        CharacterDetail.zone = MyData.instance.currentZone.id;
    }

    public void HandlePropertyEdit(String property) {

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
                return tab2;
            else if(position == 1)
                return tab1;
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
            View rootView = inflater.inflate(R.layout.activity_my_char, container, false);

            MainTab.instance.mCharImage = (ImageView) rootView.findViewById(R.id.cImage);
            MainTab.instance.mCharName= (TextView) rootView.findViewById(R.id.cName);
            MainTab.instance.mPropertyList = (LinearLayout)rootView.findViewById(R.id.list);

            rootView.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    int action = event.getAction();
                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            // do nothing
                            break;
                        case DragEvent.ACTION_DRAG_ENTERED:
                            //v.setBackgroundDrawable(enterShape);
                            break;
                        case DragEvent.ACTION_DRAG_EXITED:
                            //v.setBackgroundDrawable(normalShape);
                            break;
                        case DragEvent.ACTION_DROP:
                            // Dropped, reassign View to ViewGroup
                            View view = (View) event.getLocalState();
                            view.setVisibility(View.VISIBLE);
                            break;
                        case DragEvent.ACTION_DRAG_ENDED:
                            //v.setBackgroundDrawable(normalShape);
                        default:
                            break;
                    }
                    return true;
                }
            });

            MainTab.instance.sendUpdateView();

            return rootView;
        }
    }


    public static class TabThree extends Fragment {

        public TabThree() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_char_list, container, false);

            MainTab.instance.mCharacterList = (LinearLayout)rootView.findViewById(R.id.characterList);
            MainTab.instance.sendUpdateView();
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

    public void cancelMove(View view) {
        if(Connector.currentConnection != null){
            Connector.currentConnection.cancelMove();
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

    public void toggleHidden(View view){
        editMode = !editMode;
        updateView();
    }

    public void refreshVision(View view){
        Connector.currentConnection.requestVision();
    }
}
