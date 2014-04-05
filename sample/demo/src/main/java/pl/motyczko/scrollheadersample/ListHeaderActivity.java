package pl.motyczko.scrollheadersample;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class ListHeaderActivity extends FragmentActivity {
    public static final int FRAGMENT_BLUR = 0;
    public static final int FRAGMENT_KEN_BURNS = 1;
    public static final int FRAGMENT_MULTI_KEN_BURNS = 2;
    public static final String FRAGMENT_KEY = "fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_header);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            Fragment fragment;
            switch (extras.getInt(FRAGMENT_KEY)) {
                default:
                case FRAGMENT_BLUR: fragment = new HeaderListFragment(); break;
                case FRAGMENT_KEN_BURNS: fragment = new KenBurnsHeaderListFragment(); break;
                case FRAGMENT_MULTI_KEN_BURNS: fragment = new MultiKenBurnsHeaderListFragment(); break;
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }
}
