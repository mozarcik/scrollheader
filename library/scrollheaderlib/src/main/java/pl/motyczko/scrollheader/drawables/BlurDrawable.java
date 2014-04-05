package pl.motyczko.scrollheader.drawables;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;


public class BlurDrawable  extends Drawable {
    private final Context mApplicationContext;
    private Bitmap mBitmap;
    private float mMaxBlurRadius = 15.f;
    private RenderScript mRenderScriptSupport;
    private ScriptIntrinsicBlur mIntrisicBlurSupport;
    private float mBlurLevel;
    private Paint mPaint = new Paint();
    private Allocation mAllocation;

    private int mBlurredBitmapCacheSize = 3;

    public BlurDrawable(Bitmap bmp, Context ctx) {
        mBitmap = bmp;
        mApplicationContext = ctx.getApplicationContext();
        initRenderScriptSupport();
    }

    @Override public void draw(Canvas canvas) {
        int i = mBlurLevel == 1.f ? mBlurredBitmapCacheSize - 1 : (int) ((mBlurLevel * mBlurredBitmapCacheSize) % mBlurredBitmapCacheSize);

        mPaint.setAlpha(255);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawBitmap(mBitmaps[i], 0, 0, mPaint);
        mPaint.setAlpha((int) ((mBlurLevel - i/mBlurredBitmapCacheSize) * mBlurredBitmapCacheSize * 255));
        canvas.drawBitmap(mBitmaps[i+1], 0, 0, mPaint);
    }

    @Override public void setAlpha(int i) {
    }

    @Override public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override public int getOpacity() {
        return 1;
    }

    @Override public int getIntrinsicWidth() {
        return mBitmap.getWidth();
    }

    @Override public int getIntrinsicHeight() {
        return mBitmap.getHeight();
    }

    public void setMaxBlurRadius(float maxBlurRadius) {
        mMaxBlurRadius = maxBlurRadius;
    }

    /**
     * Blurs bitmap using specified fraction.
     *
     * @param fraction (0,1] blur fraction
     */
    public void blur(float fraction) {
        mBlurLevel = fraction;
        invalidateSelf();
    }

    private Bitmap mBitmaps[];

    private void initRenderScriptSupport() {
        mRenderScriptSupport = RenderScript.create(mApplicationContext);
        mIntrisicBlurSupport = ScriptIntrinsicBlur.create(mRenderScriptSupport, Element.U8_4(mRenderScriptSupport));

        mBitmaps = new Bitmap[mBlurredBitmapCacheSize + 1];
        mBitmaps[0] = mBitmap;
        for (int i = 1; i < mBlurredBitmapCacheSize + 1; i++) {
            mBitmaps[i] = blurBackgroundSupport(i * mMaxBlurRadius / mBlurredBitmapCacheSize);
        }

        mAllocation.destroy();
        mAllocation = null;
        mIntrisicBlurSupport.destroy();
        mIntrisicBlurSupport = null;
        mRenderScriptSupport.destroy();
        mRenderScriptSupport = null;
    }

    private Bitmap blurBackgroundSupport(float radius) {
        radius = Math.round(radius);

        if (radius <= 0.f)
            return null;

        Bitmap blurred = mBitmap.copy(mBitmap.getConfig(), true);

        mAllocation = Allocation.createFromBitmap(mRenderScriptSupport, blurred);

        mAllocation.copyFrom(mBitmap);
        mIntrisicBlurSupport.setInput(mAllocation);
        mIntrisicBlurSupport.setRadius(radius);

        mIntrisicBlurSupport.forEach(mAllocation);
        mAllocation.copyTo(blurred);
        return blurred;
    }
}
