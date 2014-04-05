/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.motyczko.scrollheader;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ListView;

import pl.motyczko.scrollheader.drawables.BlurDrawable;
import pl.motyczko.scrollheader.drawables.CircleFramedDrawable;
import pl.motyczko.scrollheader.drawables.KenBurnsDrawable;
import pl.motyczko.scrollheader.helpers.AlphaForegroundColorSpan;
import pl.motyczko.scrollheader.helpers.PageScrollHelper;
import pl.motyczko.scrollheader.helpers.PageScrollListener;
import pl.motyczko.scrollheader.helpers.SimplePageScrollListener;
import pl.motyczko.scrollheader.views.ObservableScrollView;

public class ListHeader extends FrameLayout {

    private static final String LOG_TAG = "ListHeader";
    private int mFrameColor = 0xffffffff;
    private float mStrokeWidth = 2;
    private int mFrameShadowColor = 0x80000000;
    private float mShadowRadius = 6;
    private int mHighlightColor = 0xffffffff;
    private boolean mKenBurnsEffect = false;
    private Drawable mBackgroundDrawable;
    private Matrix mDrawMatrix;
    private ActionBar mActionBar;
    private Interpolator mActionBarTitleInterpolator;

	private final PageListener mPageScrollListener = new PageListener();

    private boolean mParallaxForBackground = true;
    private Paint mPaint = new Paint();
    private boolean mBlurBackground = false;
    private boolean mKenBurnsInitialized;
    private Drawable mIcon;
    private int mIconSize = 80;
    private int mActionBarIconSize = 48;
    private SpannableString mSpannableString;
    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;


    public ListHeader(Context context) {
		this(context, null);
	}

	public ListHeader(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ListHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setWillNotDraw(false);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		mMinHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMinHeight, dm);
		mIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIconSize, dm);
		mStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mStrokeWidth, dm);
		mShadowRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mShadowRadius, dm);
        mActionBarIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mActionBarIconSize, dm);
		// get custom attrs

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListHeader);

		mMinHeight = a.getDimensionPixelSize(R.styleable.ListHeader_android_minHeight, mMinHeight);
        mBlurBackground = a.getBoolean(R.styleable.ListHeader_blurBackground, mBlurBackground);
        mKenBurnsEffect = a.getBoolean(R.styleable.ListHeader_kenBurnsEffect, mKenBurnsEffect);
        mParallaxForBackground = a.getBoolean(R.styleable.ListHeader_parallaxEffect, mParallaxForBackground);
        mIconSize = a.getDimensionPixelSize(R.styleable.ListHeader_iconSize, mIconSize);
        mIcon = a.getDrawable(R.styleable.ListHeader_android_icon);
        mFrameColor = a.getColor(R.styleable.ListHeader_frameColor, mFrameColor);
        mStrokeWidth = a.getDimension(R.styleable.ListHeader_strokeWidth, mStrokeWidth);
        mFrameShadowColor = a.getColor(R.styleable.ListHeader_frameShadowColor, mFrameShadowColor);
        mShadowRadius = a.getDimension(R.styleable.ListHeader_shadowRadius, mShadowRadius);
        mHighlightColor = a.getColor(R.styleable.ListHeader_highlightColor, mHighlightColor);

        if (mIcon instanceof BitmapDrawable) {
            mIcon = new CircleFramedDrawable(((BitmapDrawable) mIcon).getBitmap(), mIconSize,
                    mFrameColor, mStrokeWidth, mFrameShadowColor, mShadowRadius,
                    mHighlightColor);
            mIcon.setBounds(0, 0, mIconSize, mIconSize);
        }

        if (mBlurBackground && mKenBurnsEffect) {
            throw new IllegalStateException("Blur and Ken Burns effect cannot be used together!");
        }
        mBackgroundDrawable = getBackground();
        setBackgroundColor(0);

        a.recycle();
        mPageScrollHelper = new PageScrollHelper(this);
        mPageScrollHelper.setPageScrollListener(mPageScrollListener);
        if (mBlurBackground) {
            mBackgroundDrawable = new BlurDrawable(((BitmapDrawable) mBackgroundDrawable).getBitmap(), getContext());
        }
        if (mKenBurnsEffect) {
            mBackgroundDrawable = new KenBurnsDrawable(mBackgroundDrawable);
            mBackgroundDrawable.setCallback(this);
        }
    }

    public void setActionBar(ActionBar actionBar) {
        if (mIcon == null)
            return;

        mActionBar = actionBar;
        if (mActionBar == null)
            return;

        mActionBar.setIcon(R.drawable.transparent_actionbar_icon);
        mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(0xffffffff);
        mActionBarTitleInterpolator = new AccelerateInterpolator();
    }

    public void setMinHeight(int minHeight) {
        mMinHeight = minHeight;
    }

    public void setParallax(boolean parallax) {
        mParallaxForBackground = parallax;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupViews();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBackgroundDrawable instanceof KenBurnsDrawable)
            ((KenBurnsDrawable) mBackgroundDrawable).stopAnimation();
    }

    public void setupViews() {
        View v = (View) getParent();
        if (v == null) return;
        ListView listView = (ListView) v.findViewById(android.R.id.list);
        if (listView != null) {
            setListView(listView);
        }

        ObservableScrollView scrollView = (ObservableScrollView) v.findViewById(R.id.scroll_view);
        if (scrollView != null) {
            setScrollView(scrollView);
        }
    }

    public void setListView(ListView listView) {
        mPageScrollHelper.setupListView(getMeasuredHeight(), listView, true);
    }

    public void setScrollView(ObservableScrollView scrollView) {
        mPageScrollHelper.setupScrollView(getMeasuredHeight(), scrollView, true);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setupViews();
    }

    private void calculateBackgroundBounds() {
        mDrawMatrix = new Matrix();
        int dwidth = mBackgroundDrawable.getIntrinsicWidth();
        int dheight = mBackgroundDrawable.getIntrinsicHeight();

        int vwidth = getWidth();
        int vheight = getHeight();

        mBackgroundDrawable.setBounds(0, 0, dwidth, dheight);

        float scale;
        float dx = 0, dy = 0;

        if (dwidth * vheight > vwidth * dheight) {
            scale = (float) vheight / (float) dheight;
            dx = (vwidth - dwidth * scale) * 0.5f;
        } else {
            scale = (float) vwidth / (float) dwidth;
            dy = (vheight - dheight * scale) * 0.5f;
        }

        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
    }

    protected boolean verifyDrawable(Drawable who) {
        return who == mBackgroundDrawable;
    }

    private void drawBackground(Canvas canvas) {
        if (mBackgroundDrawable == null)
            return;

        if (mBackgroundDrawable instanceof KenBurnsDrawable && !mKenBurnsInitialized) {
            mBackgroundDrawable.setBounds(0,0,getWidth(), getHeight());
            ((KenBurnsDrawable) mBackgroundDrawable).animate();
            mKenBurnsInitialized = true;
        }
        if (mDrawMatrix == null && !(mBackgroundDrawable instanceof KenBurnsDrawable)) {
            calculateBackgroundBounds();
        }
        int saveCount = canvas.getSaveCount();
        canvas.save();
        float translation = mParallaxForBackground ? getTranslationY()/2 : 0;
        canvas.translate(getScrollX(), getScrollY() - translation);
        if (mDrawMatrix != null) canvas.concat(mDrawMatrix);
        mBackgroundDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);

        if (mActionBar != null) {
            setActionBarTitle();
        }
        if (mIcon != null) {
            canvas.save();
            float translationX = calculateIconTranslationX();
            float translationY = calculateIconTranslationY();
            canvas.translate(translationX, translationY);
            float scale = calculateIconScale();
            canvas.scale(scale, scale);
            mIcon.draw(canvas);
            canvas.restore();
        }
    }

    private void setActionBarTitle() {
        float fraction = Math.abs(getTranslationY()/getAllowedVerticalScrollLength());
        if (mSpannableString == null)
            mSpannableString = new SpannableString(mActionBar.getTitle());
        mAlphaForegroundColorSpan.setAlpha(mActionBarTitleInterpolator.getInterpolation(fraction));
        mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mActionBar.setTitle(mSpannableString);
    }


    private float calculateIconTranslationX() {
        int width = getWidth();

        float fraction = Math.abs(getTranslationY()/getAllowedVerticalScrollLength());

        return (1-fraction)*(width / 2 - mIconSize / 2);
    }

    private float calculateIconTranslationY() {
        int height = getHeight();
        return height / 2 - mIconSize*calculateIconScale() / 2 - getTranslationY()/2;
    }

    private float calculateIconScale() {
        float fraction = Math.abs(getTranslationY()/getAllowedVerticalScrollLength());
        float minSize = (float)mActionBarIconSize/(float)mIconSize;
        float scale = 1 - (1 - minSize) * fraction;
        return scale;
    }

    @Override
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
	}

    public void setHeight(int height) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height;
        setLayoutParams(lp);
        getParent().requestLayout();
    }


    private class PageListener extends SimplePageScrollListener {
        @Override public void onPageVerticalScroll(View v, int currentPage, int offset) {
            final float amtToScroll = Math.max(offset, -getAllowedVerticalScrollLength());
            moveToYCoordinate(amtToScroll);
        }

        @Override public void onScrollStateChanged(int currentPage, int scrollState) {
            if (scrollState == PageScrollListener.SCROLL_STATE_IDLE) {
            }
        }
    }

    private PageScrollHelper mPageScrollHelper;

    /**
     * Allowed vertical scroll length
     */
    private int mMinHeight = 0;

    private boolean mIsAnimating = false;

    /**
     * Request that the view move to the given Y coordinate. Also store the Y
     * coordinate as the last requested Y coordinate for the given tabIndex.
     *
     * @param y The Y cooridinate to move to
     */
    public void moveToYCoordinate(float y) {
        if (getTranslationY() == y || mIsAnimating)
            return;
        if (mBlurBackground && mBackgroundDrawable instanceof BlurDrawable)
            ((BlurDrawable) mBackgroundDrawable).blur(Math.abs(y/getAllowedVerticalScrollLength()));
        setTranslationY(y);
        invalidate();
    }

    /**
     * Returns the number of pixels that this view can be scrolled vertically
     * while still allowing the tab labels to still show
     */
    public int getAllowedVerticalScrollLength() {
        return getMeasuredHeight() - mMinHeight;
    }


}
