package pl.motyczko.scrollheader.helpers;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * PageScrollListener is helper class to notify about page scrolling vertical and/or horizontal.
 */
public interface PageScrollListener extends ViewPager.OnPageChangeListener{
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;
    public static final int SCROLL_STATE_FLING = 3;

    /**
     * Notifies about vertical scroll. It can be either ListView scroll (which notifies only when first item is
     * visible on screen) or ObservableScrollView scroll.
     *
     * @param currentPage Current page visible in ViewPager
     * @param offset Offset of current view (positive means scroll down and negative is scroll up )
     */
    public void onPageVerticalScroll(View v, int currentPage, int offset);

    /**
     * Notifies about scroll state change (idle, dragging, etc). It can be triggered by ListView or ViewPager.
     *
     * @param scrollState The current scroll state. The current scroll state. One of {@link #SCROLL_STATE_IDLE},
     * {@link #SCROLL_STATE_DRAGGING}, {@link #SCROLL_STATE_SETTLING} or {@link #SCROLL_STATE_FLING}.
     */
    public void onScrollStateChanged(int currentPage, int scrollState);
}
