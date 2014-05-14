/*
 *  Copyright (C) 2013 h0rn3t
 *  Modifications Copyright (C) 2013 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.widgets.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.Prop;

import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;

public class PropAdapter extends ArrayAdapter<Prop> {
    private final  Context    mContext;
    private final  List<Prop> mProps;
    private static Filter     mFilter;

    public PropAdapter(Context context, List<Prop> objects) {
        super(context, R.layout.list_item_prop, objects);
        mContext = context;
        mProps = objects;
    }

    public Prop getItem(final int i) { return mProps.get(i); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            final LayoutInflater vi =
                    (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_item_prop, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Prop p = mProps.get(position);
        if (p != null) {
            if (viewHolder.pp != null) {
                viewHolder.pp.setText(p.getName());
            }
            if (viewHolder.pv != null) {
                viewHolder.pv.setText(p.getVal());
            }

        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new AppFilter(mProps);
        }
        return mFilter;
    }

    private static class ViewHolder {
        private final TextView pp;
        private final TextView pv;

        private ViewHolder(final View rootView) {
            pp = findById(rootView, R.id.prop);
            pv = findById(rootView, R.id.pval);
        }
    }

    private class AppFilter extends Filter {
        private final List<Prop> sourceObjects;

        public AppFilter(List<Prop> props) {
            sourceObjects = new ArrayList<Prop>();
            synchronized (this) {
                sourceObjects.addAll(props);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            final String filterSeq = chars.toString().toLowerCase();
            final FilterResults result = new FilterResults();
            if (filterSeq != null && filterSeq.length() > 0) {
                final List<Prop> filter = new ArrayList<Prop>();
                for (final Prop o : mProps) {
                    if (o.getName().toLowerCase().contains(filterSeq)) { filter.add(o); }
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            final List<Prop> filtered = (List<Prop>) results.values;
            notifyDataSetChanged();
            clear();
            if (filtered != null) {
                for (final Prop aFiltered : filtered) add(aFiltered);
            }
            notifyDataSetInvalidated();
        }
    }
}
