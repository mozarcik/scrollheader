/*
 * Copyright (C) 2013 Andrew Neal
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

package pl.motyczko.scrollheadersample;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import pl.motyczko.scrollheader.ListHeader;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class HeaderListFragment extends ListFragment {
    /**
     * List content
     */
    private static final String[] MOVIES = new String[] {
            "Castle in the Sky", "Grave of the Fireflies", "My Neighbor Totoro",
            "Kiki's Delivery Service", "Only Yesterday", "Porco Rosso", "Pom Poko",
            "Whisper of the Heart", "Princess Mononoke", "My Neighbors the Yamadas",
            "Spirited Away", "The Cat Returns", "Howl's Moving Castle", "Tales from Earthsea",
            "Ponyo", "The Secret World of Arrietty", "From Up on Poppy Hill",
            "The Wind Rises", "The Tale of Princess Kaguya"
    };

    protected ListHeader mListHeader;

    /**
     * Empty constructor as per the {@link android.support.v4.app.Fragment} docs
     */
    public HeaderListFragment() {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListHeader.setActionBar(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        mListHeader.setActionBar(getActivity().getActionBar());
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.blur_header_list_fragment, container, false);
        mListHeader = (ListHeader) v.findViewById(R.id.list_header);
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Arrays.sort(MOVIES);
        final CarouselListAdapter adapter = new CarouselListAdapter(getActivity());
        for (final String movie : MOVIES) {
            adapter.add(movie);
        }

        // Bind the data
        setListAdapter(adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onListItemClick(android.widget.ListView l, android.view.View v, int position, long id) {
        // This is the header
        // Remember to substract one from the touched position
        final String movie = (String) l.getItemAtPosition(position);
        Toast.makeText(getActivity(), movie, Toast.LENGTH_SHORT).show();
    }

    /**
     * This is a special adapater used in conjunction with. In order to
     * smoothly animate the {@link }, a faux header in placed at
     * position == 0 in the adapter. This isn't necessary to use the widget, but
     * it is if you want the animation to appear correct.
     */
    private static final class CarouselListAdapter extends ArrayAdapter<String> {
        /**
         * Constructor of <code>CarouselListAdapter</code>
         *
         * @param context The {@link android.content.Context} to use
         */
        public CarouselListAdapter(Context context) {
            super(context, 0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final String movies = getItem(position);
            holder.mLineOne.get().setText(movies);
            return convertView;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getCount() {
            return MOVIES.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static final class ViewHolder {

        public WeakReference<TextView> mLineOne;

        /* Constructor of <code>ViewHolder</code> */
        public ViewHolder(View view) {
            // Initialize mLineOne
            mLineOne = new WeakReference<TextView>((TextView) view.findViewById(android.R.id.text1));
        }
    }

}
