package com.bruce.emehiser.audiorecorder;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PlaybackFragment.OnFragmentInteractionListener {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    public static final String PLAYBACK_FRAGMENT_TAG = "playback_fragment";
    public static final String RECORDING_FRAGMENT_TAG = "recording_fragment";
    public static final String EDIT_FRAGMENT_TAG = "edit_fragment";
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get the fragment manager from the system
        mFragmentManager = getFragmentManager();

        // get a new playback fragment and attach it to the frame
//        PlaybackFragment playbackFragment = new PlaybackFragment();
//        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
//        fragmentTransaction.add(R.id.main_frame, playbackFragment, PLAYBACK_FRAGMENT_TAG);
//        fragmentTransaction.commit();

//        mFragmentManager.beginTransaction().replace(R.id.main_frame, new EditFragment(), EDIT_FRAGMENT_TAG).commit();

        // add a new fragment to the frame
        mFragmentManager.beginTransaction()
                .add(R.id.main_frame, new EditFragment(), EDIT_FRAGMENT_TAG)
                .commit();

        // floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        // check read write permission
//        if(! hasReadWritePermission()) {
//            // complain to user
//            Toast.makeText(MainActivity.this, "App Requires External Storage Read Write Permission", Toast.LENGTH_LONG).show();
//            // stop the app
//            finish();
//        }
    }

    /**
     * Check file read/write permission
     * @return boolean permission status
     */
    private boolean hasReadWritePermission() {
        // check current location permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.nav_playback:
                // display playback fragment
                PlaybackFragment playbackFragment = (PlaybackFragment) mFragmentManager.findFragmentByTag(PLAYBACK_FRAGMENT_TAG);
                if(playbackFragment == null) {
                    playbackFragment = new PlaybackFragment();
                }
                mFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, playbackFragment, PLAYBACK_FRAGMENT_TAG)
                        .commit();
                break;
            case R.id.nav_record:
                // display recorder fragment
                RecorderFragment recorderFragment = (RecorderFragment) mFragmentManager.findFragmentByTag(RECORDING_FRAGMENT_TAG);
                if(recorderFragment == null) {
                    recorderFragment = new RecorderFragment();
                }
                mFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, recorderFragment, RECORDING_FRAGMENT_TAG)
                        .commit();
                break;
            case R.id.nav_edit:
                // display the edit fragment
                EditFragment editFragment = (EditFragment) mFragmentManager.findFragmentByTag(EDIT_FRAGMENT_TAG);
                if(editFragment == null) {
                    editFragment = new EditFragment();
                }
                mFragmentManager.beginTransaction()
                        .replace(R.id.main_frame, editFragment, EDIT_FRAGMENT_TAG)
                        .commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
