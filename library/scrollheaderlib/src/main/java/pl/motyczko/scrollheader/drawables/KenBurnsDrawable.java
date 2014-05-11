package pl.motyczko.scrollheader.drawables;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import java.util.Random;

/**
 * KenBurnsDrawable is drawable with Ken Burns effect animation.
 */
public class KenBurnsDrawable extends Drawable implements Animator.AnimatorListener {
    private static final String LOG_TAG = "KenBurns";
    private Drawable[] mDrawables;
    private int mCurrentDrawable = 0;
    private final Random mRandom = new Random();
    private int mSwapMs = 10000;
    private int mFadeInOutMs = 400;

    private float mMaxScaleFactor = 1.5F;
    private float mMinScaleFactor = 1F;
    private long mDuration = 10000;
    private float mScale = 1.f;

    private int mHeight;
    private int mWidth;
    private int mAlpha = 0;
    private float mTranslationX = 0.f;
    private float mTranslationY = 0.f;
    private float mNextScale;
    private float mNextTranslationX;
    private float mNextTranslationY;
    private Animator mAnimator;
    private boolean mAnimate = false;
    private ColorFilter mColorFilter;

    public KenBurnsDrawable(Drawable drawable) {
        mHeight = drawable.getIntrinsicHeight();
        mWidth = drawable.getIntrinsicWidth();
        if (drawable instanceof LayerDrawable) {
            int layerCount = ((LayerDrawable) drawable).getNumberOfLayers();
            mDrawables =  new Drawable[layerCount];
            for (int i = 0; i < layerCount; i++) {
                mDrawables[i] = ((LayerDrawable) drawable).getDrawable(i);
            }
        } else {
            mDrawables = new Drawable[]{drawable, drawable};
        }
    }

    public KenBurnsDrawable(Drawable[] drawables) {
        mDrawables = drawables;
    }

    @Override public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override public void setBounds (int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);

        for (Drawable d : mDrawables)
            d.setBounds(0, 0, mWidth, mHeight);
    }

    public boolean isAnimating() { return mAnimate; }

    public void animate() {
        mAnimator = createAnimator();
        mAnimator.start();
        mAnimate = true;
    }

    public void stopAnimation() {
        if (mAnimator != null)
            mAnimator.cancel();
        mAnimate = false;
    }

    @Override public void draw(Canvas canvas) {
//        Log.i(LOG_TAG, "draw");
        canvas.save();
        canvas.scale(mScale, mScale);
        canvas.translate(mTranslationX, mTranslationY);
        Drawable d = mDrawables[mCurrentDrawable];
        d.setAlpha(0xFF);
        d.setColorFilter(mColorFilter);
        d.draw(canvas);
        canvas.restore();
        if ( mAlpha != 0) {
            d = mDrawables[mDrawables.length == mCurrentDrawable + 1 ? 0 : mCurrentDrawable + 1];
            canvas.save();
            canvas.scale(mNextScale, mNextScale);
            canvas.translate(mNextTranslationX, mNextTranslationY);
            d.setAlpha(mAlpha);
            d.setColorFilter(mColorFilter);
            d.draw(canvas);
            d.setAlpha(0xFF);
            canvas.restore();
        }
    }

    @Override public void setAlpha(int i) {
        mAlpha = i;
        invalidateSelf();
    }

    public int getAlpha() {
        return mAlpha;
    }

    @Override public void setColorFilter(ColorFilter colorFilter) {
        mColorFilter = colorFilter;
    }

    @Override public int getOpacity() {
        return 0;
    }

    private float pickScale() {
        return mMinScaleFactor + mRandom.nextFloat() * (mMaxScaleFactor - mMinScaleFactor);
    }

    private float pickTranslation(int bounds, int intrinsic, float ratio) {
        float maxTranslation = intrinsic - bounds/ratio;
        float random = mRandom.nextFloat();
        return random * -maxTranslation;
    }

    private Animator createAnimator() {
        Rect rect = getBounds();
        Drawable nextDrawable = mDrawables[mDrawables.length == mCurrentDrawable + 1 ? 0 : mCurrentDrawable + 1];
        mWidth = nextDrawable.getIntrinsicWidth();
        mHeight = nextDrawable.getIntrinsicHeight();

        if (mWidth * rect.height() > rect.width() * mHeight) {
            mMinScaleFactor = (float) rect.height() / (float) mHeight;
        } else {
            mMinScaleFactor = (float) rect.width() / (float) mWidth;
        }

        mMaxScaleFactor = mMinScaleFactor + 0.5f;

        mNextScale = pickScale();
        float toScale = pickScale();

        mNextTranslationX = pickTranslation(rect.width(), mWidth, mNextScale);
        mNextTranslationY = pickTranslation(rect.height(), mHeight, mNextScale);
        float toTranslationX = pickTranslation(rect.width(), mWidth, toScale);
        float toTranslationY = pickTranslation(rect.height(), mHeight, toScale);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{
                ObjectAnimator.ofFloat(this, "scale", mNextScale, toScale),
                ObjectAnimator.ofFloat(this, "translationX", mNextTranslationX, toTranslationX),
                ObjectAnimator.ofFloat(this, "translationY", mNextTranslationY, toTranslationY)
        });
        animatorSet.setDuration(mDuration);
        animatorSet.addListener(this);
        return animatorSet;
    }

    public void setScale(float scale) {
        mScale = scale;
        invalidateSelf();
    }

    public float getScale() {
        return mScale;
    }

    public void setTranslationX(float translation) {
        mTranslationX = translation;
        invalidateSelf();
    }

    public float getTranslationX() {
        return mTranslationX;
    }

    public void setTranslationY(float translation) {
        mTranslationY = translation;
        invalidateSelf();
    }

    public float getTranslationY() {
        return mTranslationY;
    }

    @Override public void onAnimationStart(Animator animator) {

    }

    @Override public void onAnimationEnd(Animator animator) {
        if (!mAnimate)
            return;

        final Animator nextAnimator = createAnimator();

        ObjectAnimator anim = ObjectAnimator.ofInt(this, "alpha", 0, 0xFF).setDuration(1000);
        anim.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {

            }

            @Override public void onAnimationEnd(Animator animator) {
                mAlpha = 0;
                mCurrentDrawable = mDrawables.length == mCurrentDrawable + 1 ? 0 : mCurrentDrawable + 1;
                if (!mAnimate)
                    return;
                nextAnimator.start();
                mAnimator = nextAnimator;
            }

            @Override public void onAnimationCancel(Animator animator) {

            }

            @Override public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.start();
        mAnimator = anim;
    }

    @Override public void onAnimationCancel(Animator animator) {

    }

    @Override public void onAnimationRepeat(Animator animator) {

    }
}
