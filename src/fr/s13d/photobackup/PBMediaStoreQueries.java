/**
 * Copyright (C) 2013-2015 Stéphane Péchard.
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
package fr.s13d.photobackup;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.LinkedHashMap;
import java.util.Set;

import fr.s13d.photobackup.preferences.PreferenceWrapper;


public class PBMediaStoreQueries {

    private static final String LOG_TAG = "PBMediaStoreQueries";
    private static Context context;
    private static final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private final PreferenceWrapper prefs;


    public PBMediaStoreQueries(Context theContext) {
        context = theContext;
        //prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs = new PreferenceWrapper(theContext);
    }

    public Cursor getAllMedia() {
        String where = null;
        Cursor cursor = null;
        final String[] projection = new String[] { "_id", "_data", "date_added" };
        Set<String> buckets = prefs.getBuckets();
        if (buckets != null && buckets.size() > 0) {
            String args = TextUtils.join(", ", buckets);
            where = "bucket_id in (" + args + ")";
        }
        try {
            cursor = context.getContentResolver().query(uri, projection, where, null, "date_added DESC");
        } catch(SecurityException e) {
            e.printStackTrace();
        }
        return cursor;
    }

    // called by MediaStore if user just made a photo
    // we check, if that photo is in the list of selected buckets and return that or null
    public Integer getLastMediaIdInStore() {
        int id = 0;
        String bucketId = "";
        final String[] projection = new String[] { "_id", MediaStore.Images.Media.BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, "date_added DESC");
        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndexOrThrow("_id");
            id = cursor.getInt(idColumn);
            int bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            bucketId = cursor.getString(bucketColumn);
        }
        closeCursor(cursor);
        if (prefs.isSelectedBucket(bucketId)) {
            return id;

        } else {
            return 0;
        }
    }

    public Cursor getMediaById(Integer id) {
        String localWhere = "_id = " + id;
        final Cursor cursor = context.getContentResolver().query(uri, null, localWhere, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor;
        } else {
            return null;
        }
    }

    public LinkedHashMap<String,String> getAllBucketNames() {
        // which image properties are we querying
        String[] PROJECTION_BUCKET = {
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                "count(*) as photo_count"
        };
        // We want to order the albums by reverse chronological order. We abuse the
        // "WHERE" parameter to insert a "GROUP BY" clause into the SQL statement.
        // The template for "WHERE" parameter is like:
        //    SELECT ... FROM ... WHERE (%s)
        // and we make it look like:
        //    SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
        // The "(1)" means true. The "1,(2)" means the first two columns specified
        // after SELECT. Note that because there is a ")" in the template, we use
        // "(2" to match it.
        String BUCKET_GROUP_BY =
                "1) GROUP BY 1,(2";

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = context.getContentResolver().query(
                images, PROJECTION_BUCKET, BUCKET_GROUP_BY, null, "photo_count desc");

        LinkedHashMap<String,String> imageBuckets = new LinkedHashMap<String, String>();
        if (cur != null && cur.moveToFirst()) {
            String bucket;
            String id;
            String bucketCount;
            int bucketColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int bucketIdColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.BUCKET_ID);
            int bucketCountColumn = cur.getColumnIndex("photo_count");

            do {
                bucket = cur.getString(bucketColumn);
                id = cur.getString(bucketIdColumn);
                bucketCount = cur.getString(bucketCountColumn);
                imageBuckets.put(id, bucket + " (" + bucketCount + ")");
            } while (cur.moveToNext());
        }
        closeCursor(cur);
        return imageBuckets;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
