package csc495.potato.walk.walkpotato.UI;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import csc495.potato.walk.walkpotato.R;
import csc495.potato.walk.walkpotato.UI.Fragments.BlockedAppFragment;
import csc495.potato.walk.walkpotato.UI.Fragments.LoginFragment;
import csc495.potato.walk.walkpotato.UI.Fragments.RewardsFragment;
import csc495.potato.walk.walkpotato.UI.Fragments.SettingsFragment;
import csc495.potato.walk.walkpotato.UI.Fragments.StepStatusFragment;
import csc495.potato.walk.walkpotato.UI.NavDrawer.NavigationDrawerCallbacks;
import csc495.potato.walk.walkpotato.UI.NavDrawer.NavigationDrawerFragment;
import csc495.potato.walk.walkpotato.UI.backgroundtasks.LogReaderService;
import csc495.potato.walk.walkpotato.UI.fitlib.Client;
import csc495.potato.walk.walkpotato.UI.fitlib.History;
import csc495.potato.walk.walkpotato.UI.fitlib.Recording;
import csc495.potato.walk.walkpotato.UI.fitlib.Sensors;
import csc495.potato.walk.walkpotato.UI.fitlib.common.Display;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerCallbacks, BlockedAppFragment.OnFragmentInteractionListener {

    private static final int REQUEST_OAUTH = 1;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;
    private PackageManager packageManager;
    private String TAG = "MainActivity";
    private Client client;
    private Sensors sensors;
    private Recording recording;
    private History history;
    private boolean authInProgress = false;
    private Display display = new Display(MainActivity.class.getName()) {
        @Override
        public void show(String msg) {
            log(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        buildFitnessClient();
        if(getSharedPreferences("walkpotato_signon", Context.MODE_PRIVATE)
                .getBoolean("autosignin", false) && !client.isConnected())
        {
            client.connect();
            initializeNavDrawer();
        }
        else
        {
            Fragment fg = LoginFragment.newInstance();
            getFragmentManager().beginTransaction().add(R.id.container, fg).commit();
        }



        Intent intent = new Intent(this, LogReaderService.class);
        startService(intent);


    }

    @Override
    public void onResume() {
        super.onResume();
        packageManager = getPackageManager();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if(client!=null&&client.isConnected()) {
            Fragment fragment;
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            switch (position) {
                case 0: //step status//
                    fragment = getFragmentManager().findFragmentById(R.id.step_status);
                    if (fragment == null) {
                        fragment = new StepStatusFragment();
                    }
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case 1: //blocked apps
                    fragment = getFragmentManager().findFragmentById(R.id.blockedapp_list);
                    if (fragment == null) {
                        fragment = new BlockedAppFragment(getApplicationContext());
                    }
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();

                    break;
                case 2:
                    fragment = getFragmentManager().findFragmentById(R.id.rewards);
                    if (fragment == null) {
                        fragment = new RewardsFragment();
                    }
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case 3:
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new SettingsFragment())
                            .addToBackStack(null).commit();
                    break;
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment!=null&&mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment!=null&&!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onBlockAppFragmentInteraction(String id) {

    }
    private void initializeNavDrawer()
    {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);
        // populate the navigation drawer
        mNavigationDrawerFragment.setUserData("Potato Doe", "potatodoe@doe.com", BitmapFactory.decodeResource(getResources(), R.drawable.mascot));
        mNavigationDrawerFragment.closeDrawer();
    }
    private void buildFitnessClient()
    {
        client = new Client(this,
                new Client.Connection() {
                    @Override
                    public void onConnected() {
                        display.show("client connected");
//                we can call specific api only after GoogleApiClient connection succeeded
                        getSharedPreferences("walkpotato_signon", Context.MODE_PRIVATE).edit()
                                .putBoolean("autosignin", true).apply();
                        History.setClient(client.getClient());

                        if(mNavigationDrawerFragment==null)initializeNavDrawer();
                        Fragment fragment = getFragmentManager().findFragmentById(R.id.step_status);
                        if (fragment == null) {
                            fragment = new StepStatusFragment();
                        }
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
//                        sensors demo
                        /*initSensors();
                        display.show("list datasources");
                        sensors.listDatasourcesAndSubscribe();

//                        recording demo
                        //pagerAdapter.getItem(FitPagerAdapter.FragmentIndex.RECORDING);
                        recording = new Recording(client.getClient(), new Display(Recording.class.getName()) {
                            @Override
                            public void show(String msg) {
                                log(msg);

                                //add(FitPagerAdapter.FragmentIndex.RECORDING, msg);
//                                InMemoryLog.getInstance().add(FitPagerAdapter.FragmentIndex.RECORDING, msg);
                            }
                        });
                        recording.subscribe();
                        recording.listSubscriptions();
*/
//                        history demo



                        //history.readWeekBefore(new Date());

                    }
                },
                new Display(Client.class.getName()) {
                    @Override
                    public void show(String msg) {
                        log(msg);
                    }
                });
    }
    /*private void initSensors() {
        display.show("init sensors");
        sensors = new Sensors(client.getClient(),
                new Sensors.DatasourcesListener() {
                    @Override
                    public void onDatasourcesListed() {
                        display.show("datasources listed");
                        ArrayList<String> datasources = sensors.getDatasources();
                        for (String d:datasources) {
                            display.show(d);
                        }

                        //clear(FitPagerAdapter.FragmentIndex.DATASOURCES);
                        //addAll(FitPagerAdapter.FragmentIndex.DATASOURCES, datasources);
                    }
                },
                new Display(Sensors.class.getName()) {
                    @Override
                    public void show(String msg) {
                        log(msg);
                        //add(FitPagerAdapter.FragmentIndex.SENSORS, msg);
                    }
                });
    }*/
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            display.log("onActivityResult: REQUEST_OAUTH");
            authInProgress = false;
            if (resultCode == Activity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!client.isConnecting() && !client.isConnected()) {
                    display.log("onActivityResult: client.connect()");
                    client.connect();
                }
            }
        }
    }
    public Client getClient()
    {
        return client;
    }
}
