package pl.motyczko.scrollheadersample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.motyczko.scrollheader.ListHeader;

/**
 * Created by michal on 04.04.14.
 */
public class MultiKenBurnsHeaderListFragment extends HeaderListFragment {

    public MultiKenBurnsHeaderListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.multi_ken_burns_header_list_fragment, container, false);
        mListHeader = (ListHeader) v.findViewById(R.id.list_header);
        return v;
    }
}
