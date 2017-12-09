package com.vannakittikun.parkingmemo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    GoogleSignInAccount account;
    FragmentManager fragmentManager;
    Fragment fragment;
    MainActivity mainActivity;

    NavigationView navigationView;

    private boolean home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        account = getIntent().getParcelableExtra("GoogleAccount");

        fragmentManager = getFragmentManager();

        if(savedInstanceState == null) {
            home = true;
            fragment = new MainPortraitFragment();
            fragmentManager.beginTransaction().replace(R.id.frameContainer, fragment, "track_car").commit();
        } else {
            fragment = getFragmentManager().findFragmentByTag("track_car");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main2, menu);

        ImageView googleProfileImage = findViewById(R.id.googleProfileImage);
        TextView googleName = findViewById(R.id.googleName);
        TextView googleEmail = findViewById(R.id.googleEmail);

        Picasso.with(this).load(account.getPhotoUrl()).into(googleProfileImage);
        googleName.setText(account.getGivenName() + " " + account.getFamilyName());
        googleEmail.setText(account.getEmail());

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && home) {
            navigationView.getMenu().getItem(1).setChecked(true);
            Fragment fragment = new MainLandscapeFragment();
            fragmentManager.beginTransaction().replace(R.id.frameContainer, fragment).commit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && home){
            navigationView.getMenu().getItem(0).setChecked(true);
            Fragment fragment = new MainPortraitFragment();
            fragmentManager.beginTransaction().replace(R.id.frameContainer, fragment).commit();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            fragment = new MainPortraitFragment();
            home = true;
        } else if (id == R.id.nav_history) {
            fragment = new MainLandscapeFragment();
            home = false;
        } else if (id == R.id.nav_signout) {
            signOut();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.frameContainer, fragment).commit();

        //fragmentManager.beginTransaction().add(fragment)

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {

        SignInActivity.mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent startSignIn = new Intent(getApplicationContext(), SignInActivity.class);
                        startActivity(startSignIn);
                    }
                });
    }
}
