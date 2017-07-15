package com.faikphone.client.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faikphone.client.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by boxfox on 2017-07-16.
 */

public class SettingCategory extends RelativeLayout {
    private ViewGroup mChildContainer;
    private CharSequence title;

    public SettingCategory(Context context) {
        super(context);
        init();
    }

    public SettingCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingCategory, 0, 0);
        try {
            title = ta.getString(R.styleable.SettingCategory_title);
        } finally {
            ta.recycle();
        }
        init();
    }

    public SettingCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingCategory, 0, 0);
        try {
            title = ta.getString(R.styleable.SettingCategory_title);
        } finally {
            ta.recycle();
        }
        init();
    }

    private void init() {
        View inflatedLayout = inflate(getContext(), R.layout.setting_category_layout, this);
        if(title!=null)
            ((TextView)inflatedLayout.findViewById(R.id.title_view)).setText(title);

        //grab the reference to the container to pass children through to
        mChildContainer = (ViewGroup)inflatedLayout.findViewById(R.id.child_field);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if(mChildContainer==null){
            super.addView(child, index, params);
        }else{
            mChildContainer.addView(child, index, params);
        }
    }
}
