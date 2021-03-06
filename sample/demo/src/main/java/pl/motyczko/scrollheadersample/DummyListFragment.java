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
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class DummyListFragment extends ListFragment implements OnItemClickListener {

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

    /**
     * Empty constructor as per the {@link Fragment} docs
     */
    public DummyListFragment() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Simple ArrayAdapter
        Arrays.sort(MOVIES);
        final CarouselListAdapter adapter = new CarouselListAdapter(getActivity());
        for (final String movie : MOVIES) {
            adapter.add(movie);
        }

        // Bind the data
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = getListView();

        // Register the onItemClickListener
        listView.setOnItemClickListener(this);
        int paddingTop = getActivity().findViewById(R.id.view_pager_header).getMeasuredHeight();
        listView.setPadding(listView.getPaddingLeft(), paddingTop, listView.getPaddingRight(), listView.getPaddingBottom());
        listView.setClipToPadding(false);
        // We disable the scroll bar because it would otherwise be incorrect
        // because of the hidden
        // header
//        listView.setVerticalScrollBarEnabled(false);

//        listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // This is the header
        // Remember to substract one from the touched position
        final String movie = (String) parent.getItemAtPosition(position);
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
         * The header view
         */
        private static final int ITEM_VIEW_TYPE_HEADER = 0;

        /**
         * * The data in the list.
         */
        private static final int ITEM_VIEW_TYPE_DATA = 1;

        /**
         * Number of views (TextView, CarouselHeader)
         */
        private static final int VIEW_TYPE_COUNT = 1;

        /**
         * Constructor of <code>CarouselListAdapter</code>
         * 
         * @param context The {@link Context} to use
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

        /**
         * {@inheritDoc}
         */
        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getItemViewType(int position) {
            return ITEM_VIEW_TYPE_DATA;
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
