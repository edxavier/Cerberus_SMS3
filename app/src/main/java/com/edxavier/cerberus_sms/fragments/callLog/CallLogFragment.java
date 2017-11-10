package com.edxavier.cerberus_sms.fragments.callLog;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.edxavier.cerberus_sms.AppOperator;
import com.edxavier.cerberus_sms.BuildConfig;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.CallsRealm;
import com.edxavier.cerberus_sms.fragments.callLog.adapter.AdapterCallsRealm;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogPresenter;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogView;
import com.edxavier.cerberus_sms.fragments.callLog.di.CallLogComponent;
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
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class CallLogFragment extends Fragment implements CallLogView {

    private static final int PERMISSIONS_REQUEST_READ_CALLS = 1;
    private static final int REQUEST_CODE_SMS_DEFAULT_DIALOG = 11;
    @Inject
    CallLogPresenter presenter;
    @Inject
    FirebaseAnalytics analytics;


    AdapterCallsRealm adapter_realm;
    SlideInBottomAnimationAdapter slideAdapter;


    @BindView(R.id.swipeCallLog)
    SwipeRefreshLayout swipeCallLog;
    @BindView(R.id.empty_call_log_message)
    TextViewHelper emptyCallLogMessage;
    @BindView(R.id.adView)
    public AdView adView;
    @BindView(R.id.empty_list_layout)
    LinearLayout emptyListLayout;
    @BindView(R.id.recycler_call_list)
    public RecyclerView recyclerCallList;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.warning)
    LinearLayout warning;

    public CallLogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_call_log, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeCallLog.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadCalllog();
            }
        });
        setupInjection();
        setupRecycler();
        loadCalllog();
        if (!Prefs.getBoolean("ads_removed", false)) {
            setupAds();
        }
        checkSMSsettings();

    }

    public void checkSMSsettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Utils.thereAreBlockedSmsNumbers() && !Utils.isDefaultSmsApp(getContext())) {
                if(adView.isShown())
                    recyclerCallList.setPadding(0, 100, 0, 90);
                else
                    recyclerCallList.setPadding(0, 100, 0, 0);
                warning.setVisibility(View.VISIBLE);
            }else {
                if(adView.isShown())
                    recyclerCallList.setPadding(0, 0, 0, 90);
                else
                    recyclerCallList.setPadding(0, 0, 0, 0);
                warning.setVisibility(View.GONE);
            }
        }

        warning.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(!Telephony.Sms.getDefaultSmsPackage(getContext()).equals(getContext().getPackageName())) {
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
                recyclerCallList.setPadding(0, 0, 0, 0);
                warning.setVisibility(View.GONE);
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
        CallLogComponent component = app.getCallLogComponent(this, this);
        component.inject(this);
    }


    @Override
    public void loadCalllog() {
        if (presenter.hasReadCalllogPermission()) {
            presenter.syncCalls();
            presenter.getCallsFromRealm();
        } else {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.info)
                    .content(R.string.calls_perms_info)
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancelar)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            requestPermissions(
                                    new String[]{Manifest.permission.READ_CALL_LOG},
                                    PERMISSIONS_REQUEST_READ_CALLS);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            showEmptyMsg(true);
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
                if (Utils.thereAreBlockedSmsNumbers() && !Utils.isDefaultSmsApp(getContext())) {
                    recyclerCallList.setPadding(0, 100, 0, 90);
                    warning.setVisibility(View.VISIBLE);
                } else {
                    recyclerCallList.setPadding(0,0,0, 90);
                }
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


    private void setupRecycler() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Normal
            recyclerCallList.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            //code for landscape mode Acostado
            recyclerCallList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            recyclerCallList.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        }
        recyclerCallList.setHasFixedSize(true);
    }

    @Override
    public void setCallsData(RealmResults<CallsRealm> callsData) {
        adapter_realm = new AdapterCallsRealm(callsData, getActivity(), presenter, this);
        /*slideAdapter = new AlphaInAnimationAdapter(adapter_realm);
        slideAdapter.setDuration(250);
        slideAdapter.setInterpolator(new OvershootInterpolator(1f));
        slideAdapter.setFirstOnly(false);
*/
        recyclerCallList.setItemAnimator(new LandingAnimator());
        recyclerCallList.getItemAnimator().setAddDuration(500);
        recyclerCallList.setAdapter(adapter_realm);
        //recyclerCallList.setItemAnimator(new ScaleInAnimator());
        swipeCallLog.setRefreshing(false);
    }

    @Override
    public void requestPermission(String[] perms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_CALL_LOG},
                    PERMISSIONS_REQUEST_READ_CALLS);
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
        checkSMSsettings();
    }

    @Override
    public void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CALLS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Snackbar.make(container, getResources().getString(R.string.sync_msg), Snackbar.LENGTH_SHORT).show();
                    loadCalllog();
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
        inflater.inflate(R.menu.menu_calls, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_resync:
                Answers.getInstance().logCustom(new CustomEvent("Sincronizar Registros")
                        .putCustomAttribute("Tipo", "Llamadas"));
                if (presenter.hasReadCalllogPermission()) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.sync_msg),
                            Toast.LENGTH_LONG).show();
                    presenter.clearRecords();
                    presenter.syncCalls();
                }
                break;
            case R.id.action_delete_logs:
                try {
                    if (presenter.hasWriteCallLogPermission()) {
                        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                                .title(R.string.drawer_delete_calls)
                                .content(getString(R.string.delete_calls_content))
                                .positiveText(getResources().getString(R.string.accept))
                                .negativeText(getResources().getString(R.string.cancelar))
                                .positiveColor(getContext().getResources().getColor(R.color.md_red_700))
                                .negativeColor(getContext().getResources().getColor(R.color.md_blue_grey_700))
                                .onPositive((dialog1, which) -> {
                                    Answers.getInstance().logCustom(new CustomEvent("Eliminar Registros")
                                            .putCustomAttribute("Tipo", "Llamadas"));
                                    int deleted = presenter.clearPhoneReacords();
                                    if (deleted == 0) {
                                        Toast.makeText(getActivity(), getString(R.string.delete_calls_fail),
                                                Toast.LENGTH_LONG).show();
                                    } else if (deleted == -1) {
                                        Toast.makeText(getActivity(), getString(R.string.no_write_perms),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }).build();
                        dialog.show();
                    } else {
                        requestPermission(new String[]{Manifest.permission.WRITE_CALL_LOG});
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getString(R.string.delete_calls_exception),
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_help:
                MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.drawer_help_menu)
                        .customView(R.layout.fragment_acercade, true)
                        .positiveText("OK")
                        .positiveColor(getContext().getResources().getColor(R.color.md_orange_700))
                        .build();
                try {
                    TextViewHelper version = (TextViewHelper) dialog.getCustomView().findViewById(R.id.app_version);
                    version.setText(BuildConfig.VERSION_NAME);
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
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
}
