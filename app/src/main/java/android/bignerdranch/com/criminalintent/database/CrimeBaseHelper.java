package android.bignerdranch.com.criminalintent.database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bruno on 12/3/15.
 */
public class CrimeBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "crimeBase.sqlite";

    public CrimeBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL = "create table " + CrimeDbSchema.CrimeTable.NAME + "(" +
                "_id integer primary key autoincrement, "   +
                CrimeDbSchema.CrimeTable.Cols.UUID      + ", "   +
                CrimeDbSchema.CrimeTable.Cols.TITLE     + ", "   +
                CrimeDbSchema.CrimeTable.Cols.DATE      + ", "   +
                CrimeDbSchema.CrimeTable.Cols.SOLVED + ")";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
