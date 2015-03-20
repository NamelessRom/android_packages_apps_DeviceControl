/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;

public class LicenseFragment extends Fragment {

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_webview, container, false);

        final WebView wv = (WebView) view.findViewById(R.id.dialog_help_webview);
        wv.getSettings().setTextZoom(90);
        if(Application.get().isDarkTheme())
            wv.loadUrl("file:///android_asset/license_dark.html");
        else
            wv.loadUrl("file:///android_asset/license.html");

        return view;
    }

}
