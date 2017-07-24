package com.edxavier.cerberus_sms.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Eder Xavier Rojas on 06/10/2015.
 */
public class TextViewHelper extends AppCompatTextView {
    Typeface roboto;

    public TextViewHelper(Context context) {
        super(context);
        setRobotoRegular();
    }

    public TextViewHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRobotoRegular();
    }

    public TextViewHelper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRobotoRegular();
    }

    public void setRobotoRegular(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
        this.setTypeface(roboto);
    }


    public void setRobotoItalic(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Italic.ttf");
        this.setTypeface(roboto);
    }

    public void setRobotoBold(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
        this.setTypeface(roboto);
    }

    public void setRobotoMedium(){
        roboto = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Medium.ttf");
        this.setTypeface(roboto);
    }


}
