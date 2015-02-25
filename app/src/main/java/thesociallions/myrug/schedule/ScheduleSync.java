package thesociallions.myrug.schedule;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.thesociallions.myrug.R;
import nl.thesociallions.myrug.helper.DB;
import nl.thesociallions.myrug.helper.DBProvider;
import nl.thesociallions.myrug.helper.Networking;

public class ScheduleSync extends Service {
    private static SyncAdapterImpl sSyncAdapter = null;
    private static AccountManager mAccountManager = null;
    private static ContentResolver mContentResolver = null;
    private static Context mContext;

    public ScheduleSync() {
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

        public SyncAdapterImpl(Context context) {
            super(context, true);
            mContext = context;
            mAccountManager = AccountManager.get(context);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            try {
                if (Networking.isWalledGardenConnection()) {
                    syncResult.stats.numIoExceptions++;
                    syncResult.delayUntil = 300;
                } else {
                    final Bundle extraData = new Bundle();
                    ScheduleSync.performSync(mContext, account, extraData, authority, provider, syncResult);
                }
            } catch (OperationCanceledException e) {
                e.printStackTrace();
                syncResult.stats.numConflictDetectedExceptions++;
                syncResult.delayUntil = 300;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = null;
        ret = getSyncAdapter().getSyncAdapterBinder();
        return ret;
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (sSyncAdapter == null) sSyncAdapter = new SyncAdapterImpl(this);
        return sSyncAdapter;
    }

    private static void performSync(Context context, Account account, Bundle extras, String authority,
                                    ContentProviderClient provider, SyncResult syncResult) throws OperationCanceledException {

        String url = String.format(context.getString(R.string.schedule_url), mAccountManager.getUserData(account, "schedule_token"));
        JSONArray schedule = getSchedule(url, syncResult);
        if (schedule != null) {
            updateSchedule(schedule, syncResult);
        } else {
            syncResult.stats.numParseExceptions++;
            syncResult.delayUntil = 300;
        }

    }

    public static JSONArray getSchedule(String url, SyncResult syncResult) {
        JSONArray schedule = null;

        try {
            // Getting Schedule JSONArray
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "My RUG " + mContext.getString(R.string.app_version));
            HttpResponse response = httpclient.execute(new HttpGet(url));
            int responsecode = response.getStatusLine().getStatusCode();

            // Parsing Schedule JSONArray, if received.
            if (responsecode == 200) {                                                              // Checking on right response code, to rule out DNS errors etc.
                HttpEntity cont = response.getEntity();
                schedule = new JSONArray(EntityUtils.toString(cont));
            } else {
                syncResult.stats.numIoExceptions++;
                syncResult.delayUntil = 300;
            }

        } catch (JSONException e) {                                                                 // This exception could be something on our side, therefore we'll display an error message to the user, asking to notify us.
            syncResult.stats.numParseExceptions++;                                                  // All other exceptions are local and can not be resolved by us.
            syncResult.delayUntil = 300;
            //notifyError("Rooster werd niet correct ingelezen vanaf de server vanwege een JSON fout.");

        } catch (ClientProtocolException e) {
            syncResult.stats.numIoExceptions++;
            syncResult.delayUntil = 300;

        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            syncResult.delayUntil = 300;

        }

        // Return Schedule JSON
        return schedule;
    }

    public static void updateSchedule(JSONArray schedule, SyncResult syncResult){
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.ENGLISH);
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        JSONObject currentClass;

        // Clear up database for new items
        mContentResolver = mContext.getContentResolver();
        SQLiteOpenHelper help = new DB(mContext);
        SQLiteDatabase sb = help.getWritableDatabase();
        sb.execSQL("DROP TABLE IF EXISTS timetable"); //TODO: TRUNCATE?
        help.onCreate(sb);
        sb.close();

        // Put schedule items in database
        try {
            for (int i = 0; i < schedule.length(); i++) {
                currentClass = schedule.getJSONObject(i);
                ContentValues NewValues = new ContentValues();

                NewValues.put(DB.KEY_SUB, currentClass.getString("summary"));
                NewValues.put(DB.KEY_TYPE, currentClass.getString("categories"));
                NewValues.put(DB.KEY_LOC, currentClass.getString("location"));
                NewValues.put(DB.KEY_LOC_MAP, currentClass.getString("uid"));
                NewValues.put(DB.KEY_LOC_MAP_NAME, currentClass.getString("sequence"));

                Date theDate = dateParser.parse(currentClass.getString("dtstart"));
                Date theEnd = dateParser.parse(currentClass.getString("dtend"));
                NewValues.put(DB.KEY_DAT, dayFormat.format(theDate));
                NewValues.put(DB.KEY_STT, timeFormat.format(theDate));
                NewValues.put(DB.KEY_END, timeFormat.format(theEnd));

                mContentResolver.insert(Uri.withAppendedPath(DBProvider.CONTENT_URI, "schedule"), NewValues);
            }
            mContentResolver.notifyChange(Uri.withAppendedPath(DBProvider.CONTENT_URI, "schedule"), null);
        } catch (JSONException e) {
            //notifyError("Database insertion: JSONException");
            syncResult.stats.numParseExceptions++;
            syncResult.delayUntil = 300;
        } catch (ParseException p) {
            //notifyError("Database insertion: ParseException");
            syncResult.stats.numParseExceptions++;
            syncResult.delayUntil = 300;
        }
    }
}