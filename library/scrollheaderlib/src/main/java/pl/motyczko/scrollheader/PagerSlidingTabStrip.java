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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.Locale;

import pl.motyczko.scrollheader.drawables.BlurDrawable;
import pl.motyczko.scrollheader.drawables.CircleFramedDrawable;
import pl.motyczko.scrollheader.drawables.KenBurnsDrawable;
import pl.motyczko.scrollheader.helpers.AlphaForegroundColorSpan;
import pl.motyczko.scrollheader.helpers.PageScrollHelper;
import pl.motyczko.scrollheader.helpers.PageScrollListener;


/**
 * TODO: extend from ListHeader and add tabs as container view
 */
public class PagerSlidingTabStrip extends HorizontalScrollView {

    private static final String LOG_TAG = "PagerSlidingTabStrip";
    private Matrix mDrawMatrix;
    private class Coordinate{
        public float y = 0.0f;
    }

    public interface IconTabProvider {
		public int getPageIconResId(int position);
	}

	// @formatter:off
	private static final int[] ATTRS = new int[] {
		android.R.attr.textSize,
		android.R.attr.textColor
    };
	// @formatter:on

	private LinearLayout.LayoutParams defaultTabLayoutParams;
	private LinearLayout.LayoutParams expandedTabLayoutParams;

	private final PageListener pageListener = new PageListener();
	public OnPageChangeListener delegatePageListener;

	private LinearLayout tabsContainer;
	private ViewPager pager;

	private int tabCount;

	private int currentPosition = 0;
	private float currentPositionOffset = 0f;

	private Paint rectPaint;
	private Paint dividerPaint;

	private int indicatorColor = 0xFF666666;
	private int underlineColor = 0x1A000000;
	private int dividerColor = 0x1A000000;

	private boolean shouldExpand = false;
	private boolean textAllCaps = true;

	private int scrollOffset = 52;
	private int indicatorHeight = 8;
	private int underlineHeight = 2;
	private int tabStripHeight = 48;
	private int dividerPadding = 12;
	private int tabPadding = 24;
	private int dividerWidth = 1;

	private int tabTextSize = 12;
	private int tabTextColor = 0xFF666666;
	private Typeface tabTypeface = null;
	private int tabTypefaceStyle = Typeface.BOLD;

	private int lastScrollX = 0;

	private int tabBackgroundResId = R.drawable.background_tab;

	private Locale locale;

    private boolean mParallaxForBackground = true;



    private int mFrameColor = 0xffffffff;
    private float mStrokeWidth = 2;
    private int mFrameShadowColor = 0x80000000;
    private float mShadowRadius = 6;
    private int mHighlightColor = 0xffffffff;
    private boolean mKenBurnsEffect = false;
    private Drawable mBackgroundDrawable;
    private ActionBar mActionBar;
    private Interpolator mActionBarTitleInterpolator;

    private final PageListener mPageScrollListener = new PageListener();

    private Paint mPaint = new Paint();
    private boolean mBlurBackground = false;
    private boolean mKenBurnsInitialized;
    private Drawable mIcon;
    private int mIconSize = 80;
    private int mActionBarIconSize = 48;
    private SpannableString mSpannableString;
    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;

	public PagerSlidingTabStrip(Context context) {
		this(context, null);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setFillViewport(true);
		setWillNotDraw(false);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
		indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
		underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
		dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
		tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
		dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dividerWidth, dm);
		tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);
		tabStripHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabStripHeight, dm);
		mMinHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMinHeight, dm);
        mIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIconSize, dm);
        mStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mStrokeWidth, dm);
        mShadowRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mShadowRadius, dm);
        mActionBarIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mActionBarIconSize, dm);

        // get system attrs (android:textSize and android:textColor)

		TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

		tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
		tabTextColor = a.getColor(1, tabTextColor);

		a.recycle();

		// get custom attrs

		a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

		indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_indicatorColor, indicatorColor);
		underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_underlineColor, underlineColor);
		dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_dividerColor, dividerColor);
		tabTextColor = a.getColor(R.styleable.PagerSlidingTabStrip_tabTextColor, tabTextColor);
		indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_indicatorHeight, indicatorHeight);
		underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_underlineHeight, underlineHeight);
		tabStripHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_tabStripHeight, tabStripHeight);
		mMinHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_android_minHeight, mMinHeight);
		dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_dividerPadding, dividerPadding);
		tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_tabPaddingLeftRight, tabPadding);
		tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_tabBackground, tabBackgroundResId);
		shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_shouldExpand, shouldExpand);
		scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_scrollOffset, scrollOffset);
		textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_textAllCaps, textAllCaps);
        mBlurBackground = a.getBoolean(R.styleable.PagerSlidingTabStrip_blurBackground_a, mBlurBackground);
        mKenBurnsEffect = a.getBoolean(R.styleable.PagerSlidingTabStrip_kenBurnsEffect_a, mKenBurnsEffect);
        mParallaxForBackground = a.getBoolean(R.styleable.PagerSlidingTabStrip_parallaxEffect_a, mParallaxForBackground);
        mIconSize = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_iconSize_a, mIconSize);
        mIcon = a.getDrawable(R.styleable.PagerSlidingTabStrip_android_icon);
        mFrameColor = a.getColor(R.styleable.PagerSlidingTabStrip_frameColor_a, mFrameColor);
        mStrokeWidth = a.getDimension(R.styleable.PagerSlidingTabStrip_strokeWidth_a, mStrokeWidth);
        mFrameShadowColor = a.getColor(R.styleable.PagerSlidingTabStrip_frameShadowColor_a, mFrameShadowColor);
        mShadowRadius = a.getDimension(R.styleable.PagerSlidingTabStrip_shadowRadius_a, mShadowRadius);
        mHighlightColor = a.getColor(R.styleable.PagerSlidingTabStrip_highlightColor_a, mHighlightColor);

        a.recycle();

		rectPaint = new Paint();
		rectPaint.setAntiAlias(true);
		rectPaint.setStyle(Style.FILL);

		dividerPaint = new Paint();
		dividerPaint.setAntiAlias(true);
		dividerPaint.setStrokeWidth(dividerWidth);

		defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

		if (locale == null) {
			locale = getResources().getConfiguration().locale;
		}

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        HorizontalScrollView.LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, tabStripHeight);
        lp.gravity = Gravity.BOTTOM;
        tabsContainer.setLayoutParams(lp);
        addView(tabsContainer);

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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getParent().requestDisallowInterceptTouchEvent(true);
        View parent = (View) getParent();
        if (parent instanceof ViewPager)
            setViewPager((ViewPager) parent);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBackgroundDrawable instanceof KenBurnsDrawable)
            ((KenBurnsDrawable) mBackgroundDrawable).stopAnimation();
    }

    protected boolean verifyDrawable(Drawable who) {
        return who == mBackgroundDrawable;
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mPageScrollHelper.setupViews();
    }



    public void setViewPager(ViewPager pager) {
        this.pager = pager;
        mPageScrollHelper = new PageScrollHelper(pager, this);
        mPageScrollHelper.setPageScrollListener(pageListener);

		if (pager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}

		notifyDataSetChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.delegatePageListener = listener;
	}

	public void notifyDataSetChanged() {

		tabsContainer.removeAllViews();
        mCoordinates = new SparseArray<Coordinate>();
        int pageCount = pager.getAdapter().getCount();
        tabCount = pager.getAdapter().getCount();

		for (int i = 0; i < tabCount; i++) {

			if (pager.getAdapter() instanceof IconTabProvider) {
				addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
			} else {
				addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
			}

		}

		updateTabStyles();

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}

				currentPosition = pager.getCurrentItem();
				scrollToChild(currentPosition, 0);
			}
		});

	}

	private void addTextTab(final int position, String title) {

		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();

		addTab(position, tab);
	}

	private void addIconTab(final int position, int resId) {

		ImageButton tab = new ImageButton(getContext());
		tab.setImageResource(resId);

		addTab(position, tab);

	}

	private void addTab(final int position, View tab) {
		tab.setFocusable(true);
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(position);
			}
		});

		tab.setPadding(tabPadding, 0, tabPadding, 0);
		tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
	}

	private void updateTabStyles() {

		for (int i = 0; i < tabCount; i++) {

			View v = tabsContainer.getChildAt(i);

			v.setBackgroundResource(tabBackgroundResId);

			if (v instanceof TextView) {

				TextView tab = (TextView) v;
				tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
				tab.setTypeface(tabTypeface, tabTypefaceStyle);
				tab.setTextColor(tabTextColor);

				// setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
				// pre-ICS-build
				if (textAllCaps) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
						tab.setAllCaps(true);
					} else {
						tab.setText(tab.getText().toString().toUpperCase(locale));
					}
				}
			}
		}

	}

	private void scrollToChild(int position, int offset) {

		if (tabCount == 0) {
			return;
		}

		int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

		if (position > 0 || offset > 0) {
			newScrollX -= scrollOffset;
		}

		if (newScrollX != lastScrollX) {
			lastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}

	}

    private void calculateBackgroundBounds() {
        mDrawMatrix = new Matrix();
        int dwidth = mBackgroundDrawable.getIntrinsicWidth();
        int dheight = mBackgroundDrawable.getIntrinsicHeight();
        mBackgroundDrawable.setBounds(0, 0, dwidth, dheight);

        int vwidth = getWidth();
        int vheight = getHeight();//mParallaxForBackground ? (int) (getHeight() - getTranslationY()) : getHeight();

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
        if (mDrawMatrix != null) {
            canvas.concat(mDrawMatrix);
        }
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

        return getScrollX() + (1-fraction)*(width / 2 - mIconSize / 2);
    }

    private float calculateIconTranslationY() {
        int height = getHeight();
        float iconScale = calculateIconScale();
        float fraction = Math.abs(getTranslationY()/getAllowedVerticalScrollLength());
        return (1-fraction)*(height / 2 - mIconSize*iconScale/2) - getTranslationY();
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
        canvas.save();
        canvas.translate(0, getHeight() - tabsContainer.getHeight());
		if (isInEditMode() || tabCount == 0) {
			return;
		}

		final int height = tabsContainer.getHeight();

		// draw indicator line

		rectPaint.setColor(indicatorColor);

		// default: line below current tab
		View currentTab = tabsContainer.getChildAt(currentPosition);
		float lineLeft = currentTab.getLeft();
		float lineRight = currentTab.getRight();

		// if there is an offset, start interpolating left and right coordinates between current and next tab
		if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

			View nextTab = tabsContainer.getChildAt(currentPosition + 1);
			final float nextTabLeft = nextTab.getLeft();
			final float nextTabRight = nextTab.getRight();

			lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
			lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
		}

		canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);

		// draw underline

		rectPaint.setColor(underlineColor);
		canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);

		// draw divider

		dividerPaint.setColor(dividerColor);
		for (int i = 0; i < tabCount - 1; i++) {
			View tab = tabsContainer.getChildAt(i);
			canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
		}

        canvas.restore();
	}

	public void setIndicatorColor(int indicatorColor) {
		this.indicatorColor = indicatorColor;
		invalidate();
	}

	public void setIndicatorColorResource(int resId) {
		this.indicatorColor = getResources().getColor(resId);
		invalidate();
	}

	public int getIndicatorColor() {
		return this.indicatorColor;
	}

	public void setIndicatorHeight(int indicatorLineHeightPx) {
		this.indicatorHeight = indicatorLineHeightPx;
		invalidate();
	}

	public int getIndicatorHeight() {
		return indicatorHeight;
	}

	public void setUnderlineColor(int underlineColor) {
		this.underlineColor = underlineColor;
		invalidate();
	}

	public void setUnderlineColorResource(int resId) {
		this.underlineColor = getResources().getColor(resId);
		invalidate();
	}

	public int getUnderlineColor() {
		return underlineColor;
	}

	public void setDividerColor(int dividerColor) {
		this.dividerColor = dividerColor;
		invalidate();
	}

	public void setDividerColorResource(int resId) {
		this.dividerColor = getResources().getColor(resId);
		invalidate();
	}

	public int getDividerColor() {
		return dividerColor;
	}

	public void setUnderlineHeight(int underlineHeightPx) {
		this.underlineHeight = underlineHeightPx;
		invalidate();
	}

	public int getUnderlineHeight() {
		return underlineHeight;
	}

	public void setDividerPadding(int dividerPaddingPx) {
		this.dividerPadding = dividerPaddingPx;
		invalidate();
	}

	public int getDividerPadding() {
		return dividerPadding;
	}

	public void setScrollOffset(int scrollOffsetPx) {
		this.scrollOffset = scrollOffsetPx;
		invalidate();
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public void setShouldExpand(boolean shouldExpand) {
		this.shouldExpand = shouldExpand;
		requestLayout();
	}

	public boolean getShouldExpand() {
		return shouldExpand;
	}

	public boolean isTextAllCaps() {
		return textAllCaps;
	}

	public void setAllCaps(boolean textAllCaps) {
		this.textAllCaps = textAllCaps;
	}

	public void setTextSize(int textSizePx) {
		this.tabTextSize = textSizePx;
		updateTabStyles();
	}

	public int getTextSize() {
		return tabTextSize;
	}

	public void setTextColor(int textColor) {
		this.tabTextColor = textColor;
		updateTabStyles();
	}

	public void setTextColorResource(int resId) {
		this.tabTextColor = getResources().getColor(resId);
		updateTabStyles();
	}

	public int getTextColor() {
		return tabTextColor;
	}

	public void setTypeface(Typeface typeface, int style) {
		this.tabTypeface = typeface;
		this.tabTypefaceStyle = style;
		updateTabStyles();
	}

	public void setTabBackground(int resId) {
		this.tabBackgroundResId = resId;
	}

	public int getTabBackground() {
		return tabBackgroundResId;
	}

	public void setTabPaddingLeftRight(int paddingPx) {
		this.tabPadding = paddingPx;
		updateTabStyles();
	}

	public int getTabPaddingLeftRight() {
		return tabPadding;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		currentPosition = savedState.currentPosition;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPosition = currentPosition;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPosition;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPosition = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        ViewGroup.LayoutParams lp = super.getLayoutParams();
        if (!(lp instanceof ViewPager.LayoutParams)) return lp;

        ViewPager.LayoutParams vplp = (ViewPager.LayoutParams) lp;
        vplp.isDecor = true;
        return vplp;
    }

    private class PageListener implements PageScrollListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            currentPosition = position;
            currentPositionOffset = positionOffset;

            scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));

            invalidate();

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                Coordinate coord = mCoordinates.get(pager.getCurrentItem());
                if (coord != null && pager.findViewWithTag(coord) == null) {
                    mCoordinates.remove(pager.getCurrentItem());
                }
                restoreYCoordinate(75, pager.getCurrentItem());
                scrollToChild(pager.getCurrentItem(), 0);
            }

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
        }

        @Override public void onPageVerticalScroll(View v, int currentPage, int offset) {
            final float amtToScroll = Math.max(offset, -getAllowedVerticalScrollLength());
            moveToYCoordinate(currentPage, amtToScroll);
            v.setTag(mCoordinates.get(currentPage));
        }

        @Override public void onScrollStateChanged(int currentPage, int scrollState) {
        }
    }

    private SparseArray<Coordinate> mCoordinates;
    private PageScrollHelper mPageScrollHelper;

    /**
     * Allowed vertical scroll length
     */
    private int mMinHeight = Integer.MIN_VALUE;

    /**
     * Store this information as the last requested Y coordinate for the given
     * tabIndex.
     *
     * @param index tabIndex The tab index being stored
     * @param y The Y coordinate to move to
     */
    public void storeYCoordinate(int index, float y) {
        Coordinate coord = mCoordinates.get(index);
        if (coord == null) {
            coord = new Coordinate();
        }
        coord.y = y;
        mCoordinates.put(index, coord);
    }

    private boolean mIsAnimating = false;
    /**
     * Restore the Y position of this view to the last manually requested value.
     * This can be done after the parent has been re-laid out again, where this
     * view's position could have been lost if the view laid outside its
     * parent's bounds.
     *
     * @param duration The duration of the animation
     * @param tabIndex The index to restore
     */
    public void restoreYCoordinate(int duration, int tabIndex) {
        final float storedYCoordinate = getStoredYCoordinateForTab(tabIndex);
        if (getTranslationY() == storedYCoordinate || mIsAnimating)
            return;
        if (duration == 0) {
            setTranslationY(storedYCoordinate);
            invalidate();
            return;
        }

        mIsAnimating = true;
        final Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.anim.accelerate_decelerate_interpolator);

        final ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationY", storedYCoordinate);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {

            }

            @Override public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }

            @Override public void onAnimationCancel(Animator animation) {

            }

            @Override public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * Request that the view move to the given Y coordinate. Also store the Y
     * coordinate as the last requested Y coordinate for the given tabIndex.
     *
     * @param tabIndex The tab index being stored
     * @param y The Y cooridinate to move to
     */
    public void moveToYCoordinate(int tabIndex, float y) {
        storeYCoordinate(tabIndex, y);
        restoreYCoordinate(0, tabIndex);
    }

    /**
     * Returns the stored Y coordinate of this view the last time the user was
     * on the selected tab given by tabIndex.
     *
     * @param tabIndex The tab index use to return the Y value
     */
    public float getStoredYCoordinateForTab(int tabIndex) {
        Coordinate coord = mCoordinates.get(tabIndex);
        if (coord == null) {
            coord = new Coordinate();
            mCoordinates.put(tabIndex, coord);
        }
        return coord.y;
    }

    /**
     * Returns the number of pixels that this view can be scrolled vertically
     * while still allowing the tab labels to still show
     */
    public int getAllowedVerticalScrollLength() {
        if (mMinHeight == Integer.MIN_VALUE)
            return getMeasuredHeight() - tabStripHeight;
        else
            return getMeasuredHeight() - mMinHeight;
    }

    public PageScrollHelper getPageScrollHelper() {
        return mPageScrollHelper;
    }
}
