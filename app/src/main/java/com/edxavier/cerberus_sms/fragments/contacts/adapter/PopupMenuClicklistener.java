package com.edxavier.cerberus_sms.fragments.contacts.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;

import com.edxavier.cerberus_sms.R;

/**
 * Created by Eder Xavier Rojas on 12/07/2016.
 */
public class PopupMenuClicklistener implements PopupMenu.OnMenuItemClickListener {
    Activity activity;
    public PopupMenuClicklistener() {
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_call_contact:
                //String uri = "tel:" + contact.getContact_phone_number().replaceAll("\\s+", "");
                Intent intent = new Intent(Intent.ACTION_CALL);
                //intent.setData(Uri.parse(uri));
                return true;
            case R.id.action_write_sms:
                Log.e("EDER", "action_play_next");
                return true;
            default:
        }
        return false;
    }
}
