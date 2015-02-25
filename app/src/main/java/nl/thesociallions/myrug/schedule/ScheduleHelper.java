package nl.thesociallions.myrug.schedule;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;

import nl.thesociallions.myrug.helper.DB;

/**
 * Created by leon on 30-11-14.
 */
public class ScheduleHelper {
    public static int isClassToday(Context ctx){
        SQLiteOpenHelper data = new DB(ctx);
        SQLiteDatabase count = data.getReadableDatabase();
        Time now = new Time();
        now.setToNow();
        String datum = now.format("%d-%m-%Y");
        String tijd = now.format("%H:%M");
        String query = "SELECT * FROM timetable WHERE " + DB.KEY_DAT + " = '"+ datum +"' AND " + DB.KEY_END + " >= '" + tijd + "'";
        Cursor num = count.rawQuery(query, null);
        int isClass = num.getCount();
        num.close();
        data.close();
        return isClass;
    }
}
