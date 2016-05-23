/**
 * Copyright (C) 2013-2015
 *
 * This file is part of PhotoBackup.
 *
 * PhotoBackup is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PhotoBackup is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.s13d.photobackup.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import fr.s13d.photobackup.Log;
import fr.s13d.photobackup.PBMedia;
import fr.s13d.photobackup.PBMediaStore;
import fr.s13d.photobackup.PBService;
import fr.s13d.photobackup.preferences.PreferenceWrapper;

public class PBWifiBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "PBWifiBroadcastReceiver";
    private static boolean alreadyFired = false;

    // binding
 
    @Override
	public void onReceive(final Context context, final Intent intent) {
        if (alreadyFired) {
            return;
        }
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        boolean wifiOnly = new PreferenceWrapper(context).wifiOnly();

        Log.d(LOG_TAG, "New Intent: action=" + intent.getAction() + ", type=" + intent.getType());
		if (wifiManager.isWifiEnabled() && wifiOnly) {

            Log.i(LOG_TAG, "Wifi comes back, checking Service");
            IBinder binder = peekService(context, new Intent(context, PBService.class));
            if(binder == null) {
                return;
            }
            PBService service = ((PBService.Binder) binder).getService();
            if(service == null) {
                return;
            }
            alreadyFired = true;
            Log.i(LOG_TAG, "Media Store: " + service.getMediaStore());

            PBMediaStore mediaStore = service.getMediaStore();
            if (mediaStore == null) {
                return;
            }
            for (PBMedia media : mediaStore.getMedias()) {
                if (media.age() < 3600 * 24 * 7 && media.getState() != PBMedia.PBMediaState.SYNCED) {
                    Log.i(LOG_TAG, "Notify to send " + media.getPath());
                    service.sendMedia(media, true);
                }
            }
		}
	}
}
