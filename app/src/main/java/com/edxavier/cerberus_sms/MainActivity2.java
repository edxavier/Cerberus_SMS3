package com.edxavier.cerberus_sms;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.edxavier.cerberus_sms.fragments.AcercadeFragment;
import com.edxavier.cerberus_sms.fragments.callLog.CallLogFragment;
import com.edxavier.cerberus_sms.fragments.checkOperator.CheckOperatorFrg;
import com.edxavier.cerberus_sms.fragments.contacts.ContactFragment;
import com.edxavier.cerberus_sms.fragments.messages.MessagesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;
    @BindView(R.id.fragmentContainer)
    FrameLayout fragmentContainer;
    @BindView(R.id.contentLayout)
    CoordinatorLayout contentLayout;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.navigation_drawer_layout)
    DrawerLayout navigationDrawerLayout;

    private FragmentManager fragmentManager;

    public final static String CHECK_OPERATOR_FRG = "cof";
    public final static String CONTACTS_FRG = "cf";
    public final static String CALL_LOG_FRG = "clf";
    public final static String MESSAGES_FRG = "mf";
    public final static String ABOUT_FRG = "af";

    public final static String FRG_KEY = "tag_frg";
    public final static String FRG_TITLE = "title_frg";

    String title_frg = "";
    String currentFragment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setuppToolbar();
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            mainToolbar.setTitle(savedInstanceState.getString(FRG_TITLE));
            switch (savedInstanceState.getString(FRG_KEY)){
                case CHECK_OPERATOR_FRG:
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, new CheckOperatorFrg(), CHECK_OPERATOR_FRG)
                            .commit();
                    currentFragment = CHECK_OPERATOR_FRG;
                    title_frg = getResources().getString(R.string.drawer_operator_menu);
                    break;
                case CONTACTS_FRG:
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, new ContactFragment(), CONTACTS_FRG)
                            .commit();
                    title_frg = getResources().getString(R.string.drawer_op_contactos);
                    currentFragment = CONTACTS_FRG;
                    break;
                case MESSAGES_FRG:
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, new MessagesFragment(), MESSAGES_FRG)
                            .commit();
                    title_frg = getResources().getString(R.string.drawer_op_inbox);
                    currentFragment = MESSAGES_FRG;
                    break;
                case CALL_LOG_FRG:
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, new CallLogFragment(), CALL_LOG_FRG)
                            .commit();
                    currentFragment = CALL_LOG_FRG;
                    title_frg = getResources().getString(R.string.drawer_op_call_log);
                    break;
                case ABOUT_FRG:
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, new AcercadeFragment(), ABOUT_FRG)
                            .commit();
                    currentFragment = ABOUT_FRG;
                    title_frg = getResources().getString(R.string.about);
                    break;
                default:
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, new CheckOperatorFrg(), CHECK_OPERATOR_FRG)
                            .commit();
                    currentFragment = CHECK_OPERATOR_FRG;
                    break;
            }
        }else {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, new CheckOperatorFrg(), CHECK_OPERATOR_FRG)
                    .commit();
            currentFragment = CHECK_OPERATOR_FRG;
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FRG_KEY, currentFragment);
        outState.putString(FRG_TITLE, title_frg);
    }

    private void setuppToolbar() {
        mainToolbar.setTitle(R.string.drawer_operator_menu);
        title_frg = getResources().getString(R.string.drawer_operator_menu);
        setSupportActionBar(mainToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, navigationDrawerLayout, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navigationDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_navigation_drawer_check_operador:
                navigationDrawerLayout.closeDrawer(GravityCompat.START);
                item.setChecked(true);
                mainToolbar.setTitle(item.getTitle());
                title_frg = item.getTitle().toString();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,R.anim.fragment_exit)
                        .replace(R.id.fragmentContainer, new CheckOperatorFrg(), CHECK_OPERATOR_FRG)
                        .commit();
                currentFragment = CHECK_OPERATOR_FRG;
                break;

            case R.id.item_navigation_contacts:
                navigationDrawerLayout.closeDrawer(GravityCompat.START);
                item.setChecked(true);
                mainToolbar.setTitle(item.getTitle());
                title_frg = item.getTitle().toString();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,R.anim.fragment_exit)
                        .replace(R.id.fragmentContainer, new ContactFragment(), CONTACTS_FRG)
                        .commit();
                currentFragment = CONTACTS_FRG;
                break;

            case R.id.item_navigation_drawer_inbox:
                navigationDrawerLayout.closeDrawer(GravityCompat.START);
                item.setChecked(true);
                title_frg = item.getTitle().toString();
                mainToolbar.setTitle(item.getTitle());
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,R.anim.fragment_exit)
                        .replace(R.id.fragmentContainer, new MessagesFragment(), MESSAGES_FRG)
                        .commit();
                currentFragment = MESSAGES_FRG;

                break;

            case R.id.item_navigation_drawer_call_log:
                navigationDrawerLayout.closeDrawer(GravityCompat.START);
                title_frg = item.getTitle().toString();
                item.setChecked(true);
                mainToolbar.setTitle(item.getTitle());
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,R.anim.fragment_exit)
                        .replace(R.id.fragmentContainer, new CallLogFragment(), CALL_LOG_FRG)
                        .commit();
                currentFragment = CALL_LOG_FRG;

                break;

            case R.id.item_navigation_drawer_acerca_de:
                navigationDrawerLayout.closeDrawer(GravityCompat.START);
                title_frg = item.getTitle().toString();
                item.setChecked(true);
                mainToolbar.setTitle(item.getTitle());
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter,R.anim.fragment_exit)
                        .replace(R.id.fragmentContainer, new AcercadeFragment(), ABOUT_FRG)
                        .commit();
                currentFragment = ABOUT_FRG;
                break;

        }
        return false;
    }
}
