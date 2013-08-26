package com.rubikssolutions.cultureshockme;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	private static final String LOG_TAG = "scrollView";

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
	        int diff = (view.getBottom()-(getHeight()+getScrollY()));// Calculate the scrolldiff
	        if( diff <= 200){  // if diff is zero, then the bottom has been reached
	            Log.d(MyScrollView.LOG_TAG, "MyScrollView: Bottom has been reached" );
	            MainActivity.loadMoreButton.performClick();
	        }
	        super.onScrollChanged(l, t, oldl, oldt);
	}	

}
