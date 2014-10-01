package com.rubikssolutions.cultureshockme;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	private static final String TAG = "MyScrollView";

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
	        View view = (View) getChildAt(getChildCount()-1);
	        int diff = (view.getBottom() - (getHeight() + getScrollY()));// Calculate the scrolldiff
	        if( diff <= 20){  // if diff is zero, then the bottom has been reached
	            Log.d(TAG, "Scrollview bottom has been reached" );
	            MainActivity.loadMoreButton.performClick();
	        }
	        super.onScrollChanged(l, t, oldl, oldt);
	}	

}
