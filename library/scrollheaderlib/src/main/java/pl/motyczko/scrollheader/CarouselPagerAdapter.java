package pl.motyczko.scrollheader;

import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public abstract class CarouselPagerAdapter extends FragmentPagerAdapter {
    public CarouselPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public int getPageHeaderImageResource(int position) {
        return -1;
    }

    public Drawable getPageHeaderImageDrawable(int position) {
        return null;
    }
}
