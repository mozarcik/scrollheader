package pl.motyczko.scrollheadersample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import pl.motyczko.scrollheader.PagerSlidingTabStrip;


public class PagerTabStripActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_tab_strip);

        final Bundle blue = new Bundle();
        blue.putInt("color", Color.parseColor("#ff33b5e5"));

        // Initialize the pager adapter
        final PagerAdapter pagerAdapter = new PagerAdapter(this);
        pagerAdapter.add(DummyListFragment.class, new Bundle());
        pagerAdapter.add(ColorFragment.class, blue);
        pagerAdapter.add(DummyListFragment.class, new Bundle());
        pagerAdapter.add(ColorFragment.class, blue);
        pagerAdapter.add(DummyListFragment.class, new Bundle());
        pagerAdapter.add(ColorFragment.class, blue);

        // Initialize the pager
        final ViewPager carouselPager = (ViewPager) findViewById(R.id.carousel_pager);
        // This is used to communicate between the pager and header
        carouselPager.setAdapter(pagerAdapter);

        PagerSlidingTabStrip header = (PagerSlidingTabStrip) findViewById(R.id.view_pager_header);
        header.setActionBar(getActionBar());
    }

}
