package fr.s13d.photobackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.preference.PreferenceManager;

import fr.s13d.photobackup.preferences.PBPreferenceFragment;
import fr.s13d.photobackup.preferences.PBServerPreferenceFragment;
import okhttp3.Credentials;


public class PreferenceWrapper {
    Context context;
    public PreferenceWrapper(Context context) {
        this.context = context;
    }
    boolean serviceShouldRun() {
        return getPreferences().getBoolean(PBPreferenceFragment.PREF_SERVICE_RUNNING, false);
    }

    boolean wifiOnly() {
        String wifiOnlyString = getPreferences().getString(PBPreferenceFragment.PREF_WIFI_ONLY,
                context.getResources().getString(R.string.only_wifi_default));
        return wifiOnlyString.equals(context.getResources().getString(R.string.only_wifi));
    }

    String getPassword() {
        return getPreferences().getString(PBServerPreferenceFragment.PREF_SERVER_PASS_HASH, "");
    }

    String getServerUrl() {
        return removeFinalSlashes(getPreferences().getString(PBServerPreferenceFragment.PREF_SERVER_URL, ""));
    }

    String getHttpAuth() {
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

    boolean uploadRecentlyOnly() {
        String uploadRecentOnlyString = getPreferences().getString(PBPreferenceFragment.PREF_RECENT_UPLOAD_ONLY,
            context.getResources().getString(R.string.only_recent_upload_default));
        return  uploadRecentOnlyString.equals(context.getResources().getString(R.string.only_recent_upload));
    }
    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    ///////////
    // Utils //
    ///////////
    static public String removeFinalSlashes(String s) {
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
