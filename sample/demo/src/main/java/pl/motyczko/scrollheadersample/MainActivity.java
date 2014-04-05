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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class MainActivity extends ListActivity {

    private String[] mSamples = new String[] {
            "Carousel sample",
            "Pager Tab Strip sample",
            "Blur Header List View",
            "Ken Burns Header List View",
            "Multi Ken Burns Header List View"
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSamples));
    }

    @Override protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = null;
        switch(position) {
            case 0: intent = new Intent(this, CarouselActivity.class); break;
            case 1: intent = new Intent(this, PagerTabStripActivity.class); break;
            case 2:
                intent = new Intent(this, ListHeaderActivity.class);
                intent.putExtra(ListHeaderActivity.FRAGMENT_KEY, ListHeaderActivity.FRAGMENT_BLUR);
                break;
            case 3:
                intent = new Intent(this, ListHeaderActivity.class);
                intent.putExtra(ListHeaderActivity.FRAGMENT_KEY, ListHeaderActivity.FRAGMENT_KEN_BURNS);
                break;
            case 4:
                intent = new Intent(this, ListHeaderActivity.class);
                intent.putExtra(ListHeaderActivity.FRAGMENT_KEY, ListHeaderActivity.FRAGMENT_MULTI_KEN_BURNS);
                break;
        }
        if (intent != null)
            startActivity(intent);
    }
}
