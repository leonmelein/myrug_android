package nl.thesociallions.myrug.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DB extends SQLiteOpenHelper{

    /** Database and table setup */
    private SQLiteDatabase mDB;
    final private static String DBNAME = "schedule";
    final private static int VERSION = 11;

    /** Timetable*/
    final public static String DATABASE_TABLE = "timetable";
    public static final String KEY_ROW_ID = "_id";
    public static final String KEY_SUB = "subject";
    public static final String KEY_TYPE = "type";
    public static final String KEY_LOC = "location";
    public static final String KEY_LOC_MAP = "maplocation";
    public static final String KEY_LOC_MAP_NAME = "mapname";
    public static final String KEY_DAT = "datum";
    public static final String KEY_STT = "starttime";
    public static final String KEY_END = "endtime";

    /** Gradetable*/
    final public static String DATABASE_TABLE_GRADES = "grades";
    public static final String KEY_ROW = "_id";
    public static final String KEY_COURSE_NL = "course_nl";
    public static final String KEY_COURSE_EN = "course_en";
    public static final String KEY_COURSECODE = "coursecode";
    public static final String KEY_GRADE = "grade";
    public static final String KEY_DATE = "date";
    public static final String KEY_DISPLAYDATE = "displaydate";
    public static final String KEY_LATEST = "latest";


    /** Constructor **/
    public DB(Context context) {
        super(context, DBNAME, null, VERSION);
        this.mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String timetable = 	"create table if not exists "+ DATABASE_TABLE + " ( "
                + KEY_ROW_ID + " integer primary key autoincrement , "
                + KEY_SUB + " text not null  , "
                + KEY_TYPE + " text not null  , "
                + KEY_LOC + "  text not null  , "
                + KEY_LOC_MAP + "  text , "
                + KEY_LOC_MAP_NAME + "  text , "
                + KEY_DAT + "  text not null  , "
                + KEY_STT + "  text not null  , "
                + KEY_END + "  text not null  ) " ;

        String grades = 	"create table if not exists "+ DATABASE_TABLE_GRADES + " ( "
                + KEY_ROW + " integer primary key autoincrement , "
                + KEY_COURSE_NL + " text not null  , "
                + KEY_COURSE_EN + "  text not null  , "
                + KEY_COURSECODE + "  text not null  , "
                + KEY_GRADE + "  text not null  , "
                + KEY_DATE + "  text not null  , "
                + KEY_DISPLAYDATE + "  text not null  , "
                + KEY_LATEST + "  text not null  ) " ;

        db.execSQL(timetable);
        db.execSQL(grades);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        String sql = "DROP TABLE IF EXISTS " + DATABASE_TABLE;
        String sql2 = "DROP TABLE IF EXISTS " + DATABASE_TABLE_GRADES; //TODO: Verbeteren
        try {
            arg0.execSQL(sql);
            arg0.execSQL(sql2);
        } catch (Exception e){
            e.printStackTrace();
        }
        onCreate(arg0);
    }
}