package nosfie.easyorg.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TasksConnector extends SQLiteOpenHelper {

    public final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS tasks"
            + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT"
            + ", name TEXT"
            + ", type TEXT"
            + ", startDate TEXT"
            + ", startTime TEXT"
            + ", count INTEGER"
            + ", reminder INTEGER"
            + ", endDate TEXT"
            + ", shoppingList TEXT"
            + ", status TEXT"
            + ", currentcount INT"
            + ")";

    public TasksConnector(Context ct, String nm, SQLiteDatabase.CursorFactory cf, int vs) {
        super(ct, nm, cf, vs);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVer, int newVer) {
        DB.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(DB);
    }
}
