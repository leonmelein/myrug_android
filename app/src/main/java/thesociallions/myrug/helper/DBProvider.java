package thesociallions.myrug.helper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/** A custom Content Provider to do the database operations */
public class DBProvider extends ContentProvider{

    public static final String PROVIDER_NAME = "thesociallions.myrug.schedule";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);
    public static final Uri CONTENT_URI_SCHEDULE = Uri.parse("content://" + PROVIDER_NAME + "/schedule");
    public static final Uri CONTENT_URI_GRADES = Uri.parse("content://" + PROVIDER_NAME + "/grades");

    private static final int SCHEDULE = 1;
    private static final int COURSE = 2;
    private static final int GRADES = 3;
    private static final int GRADE = 4;

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "schedule", SCHEDULE);
        uriMatcher.addURI(PROVIDER_NAME, "schedule/#", COURSE);
        uriMatcher.addURI(PROVIDER_NAME, "grades", GRADES);
        uriMatcher.addURI(PROVIDER_NAME, "grades/#", GRADE);
    }

    /** This content provider does the database operations by this object */
    DB mDataDB;

    /** A callback method which is invoked when the content provider is starting up */
    @Override
    public boolean onCreate() {
        mDataDB = new DB(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();


        // Set the table
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case SCHEDULE:
                queryBuilder.setTables(DB.DATABASE_TABLE);
                break;
            case COURSE:
                queryBuilder.setTables(DB.DATABASE_TABLE);
                // adding the ID to the original query
                queryBuilder.appendWhere(DB.KEY_ROW_ID + "=" + uri.getLastPathSegment());
                break;
            case GRADES:
                queryBuilder.setTables(DB.DATABASE_TABLE_GRADES);
                break;
            case GRADE:
                queryBuilder.setTables(DB.DATABASE_TABLE_GRADES);
                queryBuilder.appendWhere(DB.KEY_ROW_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = mDataDB.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    //TODO: Deleten van een individueel item mogelijk maken!
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sb = mDataDB.getWritableDatabase();
        assert sb != null;
        sb.execSQL("DROP TABLE IF EXISTS "+ DB.DATABASE_TABLE);
        return 0;
    }

    //Gegevensets aan roosterdatabase toevoegen
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sb = mDataDB.getWritableDatabase();
        assert sb != null;

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case SCHEDULE:
                sb.insert(DB.DATABASE_TABLE, null, values);
                break;
            case GRADES:
                sb.insert(DB.DATABASE_TABLE_GRADES, null,values);
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
