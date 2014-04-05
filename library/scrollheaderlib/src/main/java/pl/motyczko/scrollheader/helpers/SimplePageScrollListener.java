package pl.motyczko.scrollheader.helpers;

import android.view.View;

/**
 * Simple implementation of PageScrollListener.
 */
public class SimplePageScrollListener implements PageScrollListener {
    @Override public void onPageVerticalScroll(View v, int currentPage, int offset) {
        //do nothing
    }

    @Override public void onScrollStateChanged(int currentPage, int scrollState) {
        //do nothing
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //do nothing
    }

    @Override public void onPageSelected(int position) {
        //do nothing
    }

    @Override public void onPageScrollStateChanged(int state) {
        //do nothing
    }
}
