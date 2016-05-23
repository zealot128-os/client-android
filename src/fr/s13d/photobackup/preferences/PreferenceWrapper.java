package fr.s13d.photobackup.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.preference.PreferenceManager;

import java.util.Set;

import fr.s13d.photobackup.Log;
import fr.s13d.photobackup.R;
import okhttp3.Credentials;


public class PreferenceWrapper {
    private static final String LOG_TAG = "PreferenceWrapper";
    Context context;
    public PreferenceWrapper(Context context) {
        this.context = context;
    }

    public boolean serviceShouldRun() {
        return getPreferences().getBoolean(PBPreferenceFragment.PREF_SERVICE_RUNNING, false);
    }

    public boolean wifiOnly() {
        String wifiOnlyString = getPreferences().getString(PBPreferenceFragment.PREF_WIFI_ONLY,
                context.getResources().getString(R.string.only_wifi_default));
        return wifiOnlyString.equals(context.getResources().getString(R.string.only_wifi));
    }

    public String getPassword() {
        return getPreferences().getString(PBServerPreferenceFragment.PREF_SERVER_PASS_HASH, "");
    }

    public String getServerUrl() {
        return removeFinalSlashes(getPreferences().getString(PBServerPreferenceFragment.PREF_SERVER_URL, ""));
    }

    public Set<String> getBuckets() {
        return getPreferences().getStringSet(PBPreferenceFragment.PREF_BUCKETS, null);
    }

    public boolean isSelectedBucket(String bucketId) {
        Set<String> buckets = getBuckets();
        // User has selected nothing - all buckets ok!
        if (buckets == null || buckets.size() == 0) {
            return true;
        }
        for (String bucket: buckets) {
            if(bucket.equals(bucketId)) {
                Log.d(LOG_TAG, "bucket of media is selected");
                return true;
            }
        }

        Log.d(LOG_TAG, "bucket of media is not selected");
        return false;
    }

    public String getHttpAuth() {
        final String login = getPreferences().getString(PBServerPreferenceFragment.PREF_SERVER_HTTPAUTH_LOGIN, "");
        final String pass = getPreferences().getString(PBServerPreferenceFragment.PREF_SERVER_HTTPAUTH_PASS, "");
        if(getPreferences().getBoolean(PBServerPreferenceFragment.PREF_SERVER_HTTPAUTH_SWITCH, false) &&
                !login.isEmpty() &&
                !pass.isEmpty()) {
            return Credentials.basic(login, pass);
        }  else
        {
            return null;
        }
    }

    public boolean uploadRecentlyOnly() {
        String uploadRecentOnlyString = getPreferences().getString(PBPreferenceFragment.PREF_RECENT_UPLOAD_ONLY,
            context.getResources().getString(R.string.only_recent_upload_default));
        return uploadRecentOnlyString.equals(context.getResources().getString(R.string.only_recent_upload));
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    ///////////
    // Utils //
    ///////////
    static private String removeFinalSlashes(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        int count = 0;
        int length = s.length();
        while (s.charAt(length - 1 - count) == '/') {
            count++;
        }

        return s.substring(0, s.length() - count);
    }
}
