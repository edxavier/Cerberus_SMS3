package com.edxavier.cerberus_sms.fragments.contacts;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.cerberus_sms.AppOperator;
import com.edxavier.cerberus_sms.DialerActivityV2;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.fragments.contacts.adapter.AdapterContactRealm;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsPresenter;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsView;
import com.edxavier.cerberus_sms.fragments.contacts.di.ContactsComponent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FlipInBottomXAnimator;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment implements ContactsView {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    AlphaInAnimationAdapter slideAdapter;

    @BindView(R.id.contacts_container)
    FrameLayout contactsContainer;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    // custom view dialog

    @Inject
    ContactsPresenter presenter;
    @Inject
    FirebaseAnalytics analytics;

    @BindView(R.id._recycler_contacts_list)
    RecyclerView RecyclerContactsList;
    @BindView(R.id.empty_list_layout)
    LinearLayout emptyListLayout;
    @BindView(R.id.adView)
    public AdView adView;

    FrameLayout frameLayout;
    private AdapterContactRealm adapter_realm;
    String query;
    private SearchView searchView;
    private boolean isFirstTime;

    public ContactFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_contactos, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupInjection();
        setupRecycler();
        if(!Prefs.getBoolean("ads_removed", false)) {
            setupAds();
        }
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncContacts();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.md_orange_500, R.color.md_cyan_500,
                R.color.md_pink_500, R.color.md_purple_500);
        frameLayout = (FrameLayout) getActivity().findViewById(R.id.fragmentContainer);
        syncContacts();
    }

    private void setupRecycler() {
        int orientation=this.getResources().getConfiguration().orientation;
        if(orientation== Configuration.ORIENTATION_PORTRAIT){
            //Normal
            RecyclerContactsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        else{
            //code for landscape mode Acostado
            RecyclerContactsList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            RecyclerContactsList.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        }
        RecyclerContactsList.setHasFixedSize(true);

    }

    private void setupAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void setupInjection() {
        AppOperator app = (AppOperator) getActivity().getApplication();
        ContactsComponent component = app.getContactComponent(this, this);
        component.inject(this);
    }

    @Override
    public void showEmptyListMessage(boolean show_empty_msg) {
        if (show_empty_msg) {
            emptyListLayout.setVisibility(View.VISIBLE);
        } else {
            emptyListLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void setContacts(RealmResults<ContactRealm> contacts) {
        adapter_realm = new AdapterContactRealm(contacts, getActivity(), presenter, this);
        slideAdapter = new AlphaInAnimationAdapter(adapter_realm);
        slideAdapter.setDuration(250);
        slideAdapter.setInterpolator(new OvershootInterpolator(1f));
        slideAdapter.setFirstOnly(false);
        RecyclerContactsList.setItemAnimator(new FlipInBottomXAnimator());
        RecyclerContactsList.getItemAnimator().setAddDuration(500);
        RecyclerContactsList.setAdapter(adapter_realm);
        RecyclerContactsList.setItemAnimator(new ScaleInAnimator());
        swipeContainer.setRefreshing(false);
    }


    public void syncContacts(){
        if(presenter.canReadContacts()) {
            presenter.syncContacts();
            presenter.getContactsFromRealm();
        }else {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.save_info)
                    .content(R.string.contact_perms_info)
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancelar)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                    PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            showEmptyListMessage(true);
                            Snackbar.make(contactsContainer,getResources().getString(R.string.no_read_perms),
                                    Snackbar.LENGTH_INDEFINITE).show();
                        }
                    })
                    .show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Snackbar.make(contactsContainer, getResources().getString(R.string.sync_contacts_msg), Snackbar.LENGTH_SHORT).show();
                    syncContacts();
                }
                break;
            }

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_contact, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        try {

            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setQueryHint(getActivity().getResources().getString(R.string.search_contact_toolbar));
            isFirstTime = true;

            RxSearchView.queryTextChanges(searchView)
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(charSequence -> {
                            if (!charSequence.toString().isEmpty()) {
                                isFirstTime = false;
                                presenter.getFilterContactsFromRealm(charSequence.toString().trim());
                            }else if(!isFirstTime){
                                presenter.getContactsFromRealm();
                            }
            });
        }catch (Exception ignored){}

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }



}