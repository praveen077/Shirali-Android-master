package com.shirali.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by Sagar on 4/8/17.
 */
public class MyScrollview extends ScrollView {

    private boolean enableScrolling = true;

    public MyScrollview(Context context) {
        super(context);
    }

    public MyScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return enableScrolling;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {

        this.enableScrolling = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (isNestedScrollingEnabled()) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isNestedScrollingEnabled()) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }
}