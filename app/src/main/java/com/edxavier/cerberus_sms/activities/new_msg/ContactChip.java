package com.edxavier.cerberus_sms.activities.new_msg;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

/**
 * Created by Eder Xavier Rojas on 20/09/2017.
 */

public class ContactChip implements ChipInterface {
    @Override
    public Object getId() {
        return null;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return "Prueba";
    }

    @Override
    public String getInfo() {
        return null;
    }
}
