package csc495.potato.walk.walkpotato.UI;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import csc495.potato.walk.walkpotato.R;
import csc495.potato.walk.walkpotato.UI.Dialogs.AppSelectDialog;
import csc495.potato.walk.walkpotato.UI.Fragments.BlockedAppFragment;
import csc495.potato.walk.walkpotato.UI.Fragments.StepStatusFragment;
import csc495.potato.walk.walkpotato.UI.NavDrawer.NavigationDrawerCallbacks;
import csc495.potato.walk.walkpotato.UI.NavDrawer.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerCallbacks, BlockedAppFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;
    private PackageManager packageManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);
        // populate the navigation drawer
        mNavigationDrawerFragment.setUserData("Potato Doe", "potatodoe@doe.com", BitmapFactory.decodeResource(getResources(), R.drawable.avatar));

        Fragment fg = StepStatusFragment.newInstance();
        getFragmentManager().beginTransaction().add(R.id.container, fg).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        packageManager = getPackageManager();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();
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
            case 2: //rewards //todo
                break;
            case 3: //settings //todo
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
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

    public void showDialog(View view) {

        FragmentManager manager = getFragmentManager();

        AppSelectDialog dialog = new AppSelectDialog(getApplicationContext());
        dialog.show(manager, "dialog");

    }
}
