package com.edxavier.cerberus_sms.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.edxavier.cerberus_sms.fragments.callLog.receivers.CallHandler;

/**
 * Created by Eder Xavier Rojas on 15/10/2015.
 */
public class skeleton_frag extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        switch (item.getItemId()) {
            case R.id.action_refresh:
                ((HomeActivity)getActivity()).loadRecentEntries(true);
                return true;
        }
                        */

        return super.onOptionsItemSelected(item);
    }



}
