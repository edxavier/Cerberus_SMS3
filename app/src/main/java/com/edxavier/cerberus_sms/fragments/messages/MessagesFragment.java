package com.edxavier.cerberus_sms.fragments.messages;


import android.Manifest;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.edxavier.cerberus_sms.AppOperator;
import com.edxavier.cerberus_sms.BuildConfig;
import com.edxavier.cerberus_sms.CreateMessageActivity;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.fragments.messages.adapter.AdapterMessagesRealm;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesPresenter;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesView;
import com.edxavier.cerberus_sms.fragments.messages.di.MessageComponent;
import com.edxavier.cerberus_sms.helpers.TextViewHelper;
import com.edxavier.cerberus_sms.helpers.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.easyprefs.library.Prefs;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FlipInBottomXAnimator;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment implements MessagesView {

    private static final int PERMISSIONS_REQUEST_READ_SMS = 1;
    @Inject
    MessagesPresenter presenter;
    @Inject
    FirebaseAnalytics analytics;
    Bundle analitycParams;


    @BindView(R.id.recyclerMessages)
    public RecyclerView recyclerMessages;
    @BindView(R.id.swipeMessages)
    SwipeRefreshLayout swipeMessages;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.empty_list_layout)
    LinearLayout emptyListLayout;
    @BindView(R.id.adView)
    public AdView adView;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.warning)
    LinearLayout warning;
    AdapterMessagesRealm adapter;
    SlideInBottomAnimationAdapter slideAdapter;
    private static final int REQUEST_CODE_SMS_DEFAULT_DIALOG = 11;
    @BindView(R.id.fab_createevent)
    FloatingActionButton fabCreateevent;

    Menu menu;
    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeMessages.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSMS();
            }
        });
        setupInjection();
        loadSMS();
        if (!Prefs.getBoolean("ads_removed", false)) {
            setupAds();
        }
        checkSMSsettings();
        fabCreateevent.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), CreateMessageActivity.class);
            getContext().startActivity(myIntent);
        });
        recyclerMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    // Scroll Down
                    if (fabCreateevent.isShown()) {
                        fabCreateevent.hide();
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!fabCreateevent.isShown()) {
                        fabCreateevent.show();
                    }
                }
            }
        });
    }

    public void checkSMSsettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Utils.thereAreBlockedSmsNumbers() && !Utils.isDefaultSmsApp(getContext())) {
                recyclerMessages.setPadding(0, 100, 0, 0);
                warning.setVisibility(View.VISIBLE);
            } else {
                if(adView.isShown())
                    recyclerMessages.setPadding(0, 0, 0, 90);
                else
                    recyclerMessages.setPadding(0, 0, 0, 0);
                warning.setVisibility(View.GONE);
            }
        }

        warning.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!Telephony.Sms.getDefaultSmsPackage(getContext()).equals(getContext().getPackageName())) {
                    //Store default sms package name
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                            getContext().getPackageName());
                    //startActivity(intent);
                    startActivityForResult(intent, REQUEST_CODE_SMS_DEFAULT_DIALOG);
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SMS_DEFAULT_DIALOG) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                recyclerMessages.setPadding(0, 0, 0, 0);
                warning.setVisibility(View.GONE);
                if(menu!=null){
                    MenuItem item = menu.findItem(R.id.action_delete_logs);
                    if(item!=null)
                        item.setVisible(true);
                }
                try {
                    Answers.getInstance().logContentView(new ContentViewEvent()
                            .putContentName("Default SMS app")
                            .putContentId("setDefaultAppDialog")
                            .putContentType("Change default app"));
                }catch (Exception ignored){}
            }
        }

    }

    @Override
    public void setupInjection() {
        AppOperator app = (AppOperator) getActivity().getApplication();
        MessageComponent component = app.getMessageComponent(this, this);
        component.inject(this);
    }

    @Override
    public void loadSMS() {
        if (presenter.hasReadSMSPermission()) {
            presenter.syncMessages();
            presenter.getMessagesFromRealm();
        } else {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.save_info)
                    .content(R.string.sms_perms_info)
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancelar)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                                    PERMISSIONS_REQUEST_READ_SMS);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            showEmptyMsg(true);
                            Snackbar.make(container, getResources().getString(R.string.no_read_perms),
                                    Snackbar.LENGTH_INDEFINITE).show();
                        }
                    })
                    .show();

        }
    }

    @Override
    public void setupAds() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                recyclerMessages.setPadding(0,0,0,90);
            }
        });
        adView.loadAd(adRequest);
    }

    @Override
    public void showEmptyMsg(boolean show) {
        if (show) {
            emptyListLayout.setVisibility(View.VISIBLE);
        } else {
            emptyListLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showProgress(boolean show) {
        if (show) {
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            loadingLayout.setVisibility(View.GONE);
        }
    }


    @Override
    public void setMessages(RealmResults<MessagesRealm> messages) {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            recyclerMessages.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            recyclerMessages.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        }
        //recyclerMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerMessages.setHasFixedSize(true);
        adapter = new AdapterMessagesRealm(messages, this, presenter);
        slideAdapter = new SlideInBottomAnimationAdapter(adapter);
        slideAdapter.setDuration(500);
        slideAdapter.setInterpolator(new OvershootInterpolator(1f));
        recyclerMessages.setItemAnimator(new FlipInBottomXAnimator());
        recyclerMessages.getItemAnimator().setAddDuration(500);
        recyclerMessages.setAdapter(adapter);
        swipeMessages.setRefreshing(false);
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
        checkSMSsettings();
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Snackbar.make(container, getResources().getString(R.string.sync_msg), Snackbar.LENGTH_SHORT).show();
                    loadSMS();
                } else {
                    showEmptyMsg(true);
                    Snackbar.make(container, getResources().getString(R.string.perm_denied), Snackbar.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_msgs, menu);
        this.menu = menu;
        if(!Utils.isDefaultSmsApp(getContext())) {
            MenuItem item = menu.findItem(R.id.action_delete_logs);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_resync:
                Answers.getInstance().logCustom(new CustomEvent("Sincronizar Registros")
                        .putCustomAttribute("Tipo", "Mensajes"));
                presenter.clearRecords();
                presenter.syncMessages();
                break;
            case R.id.action_delete_logs:
                    MaterialDialog dlg = new MaterialDialog.Builder(getContext())
                            .title(R.string.notice)
                            .content("Esta accion eliminara todos los sms tanto de la aplicacion como del telefono, deseas continuar?")
                            .positiveText(R.string.accept)
                            .negativeText(R.string.cancelar)
                            .onPositive((dialog1, which) -> {
                                Answers.getInstance().logCustom(new CustomEvent("Eliminar Registros")
                                        .putCustomAttribute("Tipo", "Mensajes"));
                                presenter.clearRecords();
                                presenter.clearPhoneReacords();
                            })
                            .build();
                    dlg.show();
                break;
            case R.id.action_mark_all_as_readed:
                presenter.markAllAsRead();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getContext());
                    managerCompat.cancelAll();
                }
                break;
            case R.id.action_help:
                MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.drawer_help_menu)
                        .customView(R.layout.fragment_acercade, true)
                        .positiveText(R.string.accept)
                        .positiveColor(getContext().getResources().getColor(R.color.md_orange_700))
                        .build();
                TextViewHelper version = (TextViewHelper) dialog.getCustomView().findViewById(R.id.app_version);
                version.setText(BuildConfig.VERSION_NAME);
                dialog.show();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
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


    public int getUnreadedMessages(){
        return presenter.getUnreadedMsgs();
    }

}
