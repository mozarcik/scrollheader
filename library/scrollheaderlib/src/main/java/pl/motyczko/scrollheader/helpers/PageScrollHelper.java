package pl.motyczko.scrollheader.helpers;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import pl.motyczko.scrollheader.R;
import pl.motyczko.scrollheader.views.ObservableScrollView;

/**
 *
 */
public class PageScrollHelper implements AbsListView.OnScrollListener,
        ObservableScrollView.OnScrollListener, ViewPager.OnPageChangeListener,
        ViewGroup.OnHierarchyChangeListener {

    private final View mHeaderView;
    /**
     * Scroll listener which will be notified about page scroll (vertical, horizontal and page change)
     */
    private PageScrollListener mPageScrollListener;

    private int mCurrentPage = 0;
    private ViewPager mViewPager;
    private Object mTag = new Object();

    public PageScrollHelper(View headerView) {
        mHeaderView = headerView;
    }

    public PageScrollHelper(ViewPager pager, View headerView) {
        mHeaderView = headerView;
        mViewPager = pager;
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setPageTransformer(false, new BasicPageTransformer());
        mViewPager.setOnHierarchyChangeListener(this);
        mCurrentPage = mViewPager.getCurrentItem();
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void setPageScrollListener(PageScrollListener listener) {
        mPageScrollListener = listener;
    }

    /**
     * ViewPager notifies about page scroll (horizontal)
     *
     * @param position current page visible on ViewPager
     * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPageScrollListener == null || mViewPager.isFakeDragging() || mCurrentPage < 0)
            return;

        mPageScrollListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    /**
     * ViewPager notifies about page change
     *
     * @param position current page visible on ViewPager
     */
    @Override public void onPageSelected(int position) {
        mCurrentPage = position;
        if (mPageScrollListener == null || mCurrentPage < 0)
            return;

        mPageScrollListener.onPageSelected(mCurrentPage);
    }

    /**
     * ViewPager notifies when page scrolling started/ended
     * @param state Scroll state
     */
    @Override public void onPageScrollStateChanged(int state) {
        if (mPageScrollListener == null || mCurrentPage < 0)
            return;

        mPageScrollListener.onPageScrollStateChanged(state);
    }

    /**
     * ObservableScrollView notifies about view scroll (vertical)
     *
     * @param view ObservableScrollView which notifies about scroll
     * @param left left position of view
     * @param top top position of view
     */
    @Override public void onScrollChanged(ObservableScrollView view, int left, int top) {
        if (mPageScrollListener == null || mCurrentPage < 0)
            return;

        mPageScrollListener.onPageVerticalScroll(view, mCurrentPage, -top);
    }

    /**
     * AbsListView notifies when scroll state changes (start, end etc)
     *
     * @param absListView The view whose scroll state is being reported
     * @param scrollState The current scroll state. The current scroll state. One of {@link android.widget.AbsListView.OnScrollListener#SCROLL_STATE_IDLE},
     * {@link android.widget.AbsListView.OnScrollListener#SCROLL_STATE_TOUCH_SCROLL} or {@link android.widget.AbsListView.OnScrollListener#SCROLL_STATE_IDLE}.
     */
    @Override public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if (mPageScrollListener == null || mCurrentPage < 0)
            return;

        mPageScrollListener.onScrollStateChanged(mCurrentPage, scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING
                ? PageScrollListener.SCROLL_STATE_FLING : scrollState);
    }

    /**
     * AbsListView notifies when list view is scrolled (vertically)
     *
     * @param absListView The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell (ignore if visibleItemCount == 0)
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount the number of items in the list adapter
     */
    @Override public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mPageScrollListener == null || mCurrentPage < 0)
            return;

        if (firstVisibleItem != 0) {
            mPageScrollListener.onPageVerticalScroll(absListView, mCurrentPage, -absListView.getPaddingTop());
            return;
        }

        final View topView = absListView.getChildAt(0);
        if (topView == null) {
            return;
        }

        final int y = topView.getTop() - absListView.getPaddingTop();
        mPageScrollListener.onPageVerticalScroll(absListView, mCurrentPage, y);
    }

    public boolean startDragging() {
        return !(mViewPager == null || mViewPager.isFakeDragging()) && mViewPager.beginFakeDrag();
    }

    public boolean endDragging() {
        if (mViewPager == null || !mViewPager.isFakeDragging())
            return false;

        try {
            mViewPager.endFakeDrag();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean dragBy(int offset) {
        if (mViewPager == null || !mViewPager.isFakeDragging())
            return false;

        mViewPager.fakeDragBy(offset);
        return true;
    }

    public void setCurrentPage(int currentPage) {
        if (mViewPager == null)
            return;

        mViewPager.setCurrentItem(currentPage, true);
        mCurrentPage = currentPage;
    }

    public void setupListView(int paddingTop, ListView listView) {
        setupListView(paddingTop, listView, false);
    }

    public void setupListView(int paddingTop, ListView listView, boolean force) {
        if (listView.getTag(R.id.page_tag) == mTag && !force)
            return;
        listView.setPadding(listView.getPaddingLeft(), paddingTop, listView.getPaddingRight(), listView.getPaddingBottom());
        listView.setClipToPadding(false);
        listView.setOnScrollListener(this);
        listView.setTag(R.id.page_tag, mTag);
        listView.scrollTo(0,0);
    }

    public void setupScrollView(int paddingTop, ObservableScrollView scrollView) {
        setupScrollView(paddingTop, scrollView, false);
    }

    public void setupScrollView(int paddingTop, ObservableScrollView scrollView, boolean force) {
        if (scrollView.getTag(R.id.page_tag) == mTag && !force)
            return;
        scrollView.setPadding(scrollView.getPaddingLeft(), paddingTop, scrollView.getPaddingRight(), scrollView.getPaddingBottom());
        scrollView.setClipToPadding(false);
        scrollView.setOnScrollListener(this);
        scrollView.setTag(R.id.page_tag, mTag);
        scrollView.scrollTo(0,0);
    }

    @Override public void onChildViewAdded(View view, View view2) {
        int childCount = mViewPager.getChildCount();
        mViewPager.bringChildToFront(mHeaderView);
        for (int i = 0; i < childCount; i++) {
            View page = mViewPager.getChildAt(i);
            View listView = page.findViewById(android.R.id.list);

            if (listView != null && listView instanceof ListView)
                setupListView(mHeaderView.getMeasuredHeight(), (ListView) listView);

            View scrollView = mViewPager.findViewById(R.id.scroll_view);
            if (scrollView != null && scrollView instanceof ObservableScrollView)
                setupScrollView(mHeaderView.getMeasuredHeight(), (ObservableScrollView) scrollView);
        }
    }

    @Override public void onChildViewRemoved(View view, View view2) {

    }

    public void setupViews() {
        onChildViewAdded(mViewPager, mHeaderView);
    }
}

