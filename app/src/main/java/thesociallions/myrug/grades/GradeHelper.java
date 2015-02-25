package thesociallions.myrug.grades;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.thesociallions.myrug.R;
import nl.thesociallions.myrug.helper.Constants;
import nl.thesociallions.myrug.helper.DB;
import nl.thesociallions.myrug.helper.DBProvider;

/**
 * Logic for Grade Processing
 * Created by leon on 23-12-14.
 */

public class GradeHelper {

    public static void updateGrades(Context ctx, JSONObject grades) throws JSONException {
        if (ctx != null) {
            SQLiteOpenHelper help = new DB(ctx);
            SQLiteDatabase sb = help.getWritableDatabase();
            sb.execSQL("DROP TABLE IF EXISTS grades"); //TODO: FIX THIS
            help.onCreate(sb);
            sb.close();

            JSONObject grades_and_courses = grades.getJSONObject("overzicht");
            JSONArray gradesList = grades_and_courses.getJSONArray("vak");

            for (int i = 0; i < gradesList.length(); i++) {
                JSONObject course = gradesList.getJSONObject(i);

                //Get title
                JSONArray titles = course.getJSONArray("titel");
                String title_nl = (titles.getJSONObject(0)).getString("content");
                String title_en = (titles.getJSONObject(1)).getString("content");

                //Get course code
                String coursecode = course.getString("vakcode");

                //Get grades
                JSONObject grades_list = course.getJSONObject("cijfers");
                try {
                    JSONObject grade_description = grades_list.getJSONObject("cijfer");
                    String grade = grade_description.getString("beoordeling");
                    String date = grade_description.getString("datum");
                    String latest = grade_description.getString("islaatste");

                    String[] separated = date.split("-");
                    String displaydate = separated[2] + "-" + separated[1] + "-" + separated[0] + " —";

                    // DEBUG: GRADES DB
                    ContentValues NewValues = new ContentValues();
                    NewValues.put(DB.KEY_COURSE_NL, title_nl);
                    NewValues.put(DB.KEY_COURSE_EN, title_en);
                    NewValues.put(DB.KEY_COURSECODE, coursecode);
                    NewValues.put(DB.KEY_GRADE, grade);
                    NewValues.put(DB.KEY_DATE, date);
                    NewValues.put(DB.KEY_DISPLAYDATE, displaydate);
                    NewValues.put(DB.KEY_LATEST, latest);
                    ctx.getContentResolver().insert(Uri.withAppendedPath(DBProvider.CONTENT_URI, "grades"), NewValues);

                } catch (JSONException J) {
                    JSONArray allGrades = grades_list.getJSONArray("cijfer");
                    for (int y = 0; y < allGrades.length(); y++) {
                        JSONObject grade_description = allGrades.getJSONObject(y);
                        String grade = grade_description.getString("beoordeling");
                        String date = grade_description.getString("datum");
                        String latest = grade_description.getString("islaatste");

                        String[] separated = date.split("-");
                        String displaydate = separated[2] + "-" + separated[1] + "-" + separated[0] + " —";

                        // DEBUG: GRADES DB
                        ContentValues NewValues = new ContentValues();
                        NewValues.put(DB.KEY_COURSE_NL, title_nl);
                        NewValues.put(DB.KEY_COURSE_EN, title_en);
                        NewValues.put(DB.KEY_COURSECODE, coursecode);
                        NewValues.put(DB.KEY_GRADE, grade);
                        NewValues.put(DB.KEY_DATE, date);
                        NewValues.put(DB.KEY_DISPLAYDATE, displaydate);
                        NewValues.put(DB.KEY_LATEST, latest);
                        ctx.getContentResolver().insert(Uri.withAppendedPath(DBProvider.CONTENT_URI, "grades"), NewValues);

                    }
                }
            }
            ctx.getContentResolver().notifyChange(Uri.withAppendedPath(DBProvider.CONTENT_URI, "grades"), null);
        }

    }


    public static JSONObject retrieveGrades(Context ctx, String devicetoken, String usertoken) throws JSONException{
        // Build request url
        String theURL = String.format(ctx.getString(R.string.grades_url),devicetoken,usertoken);
        Log.e(Constants.TAG, theURL);

        // Get grades in JSON
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(theURL));
            HttpEntity cont = response.getEntity();
            String grades = EntityUtils.toString(cont);
            return new JSONObject(grades);

        } catch (Exception e) {
            throw new JSONException("Could not make JSON from Grades");
        }
    }

}
