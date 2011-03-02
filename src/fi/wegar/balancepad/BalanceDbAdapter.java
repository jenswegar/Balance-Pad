package fi.wegar.balancepad;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Simple balance database access helper class. Defines the basic CRUD operations
 * for the Balance Pad application, and gives the ability to list all entries as well as
 * retrieve or modify a specific entry.
 */
public class BalanceDbAdapter {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_CURRENCY = "currency";
    public static final String KEY_EVENT_DATE = "event_date";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "BalanceDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
    	"create table balance (_id integer primary key autoincrement, "
        + "description text not null, amount float not null, currency text not null, " +
        		"event_date long not null);";

    private static final String DATABASE_NAME = "balance_db";
    private static final String DATABASE_TABLE = "balance";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will add the currency column with default value EUR");
            
            db.execSQL("ALTER TABLE balance ADD COLUMN currency TEXT NOT NULL DEFAULT 'EUR'");
            
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public BalanceDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public BalanceDbAdapter open() throws SQLException {
    	// make sure only one mDbHelper is ever created
        mDbHelper = (mDbHelper == null) ? new DatabaseHelper(mCtx) : mDbHelper;
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new balance entry using the description. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param description the description of the balance entry
     * @param amount the amount that the balance entry cost
     * @param currency the currency of the balance entry
     * @param eventDate the date that the transaction happened
     * @return rowId or -1 if failed
     */
    public long createEntry(String description, double amount, String currency, Date eventDate) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DESCRIPTION, description);
        initialValues.put(KEY_AMOUNT, amount);
        initialValues.put(KEY_CURRENCY, currency);
        initialValues.put(KEY_EVENT_DATE, eventDate.getTime() );

        long rtn = mDb.insert(DATABASE_TABLE, null, initialValues);
        if(rtn > -1)
        {
        	Toast.makeText(mCtx, R.string.entry_saved, Toast.LENGTH_SHORT).show();
        }
        	
        return rtn;
    }

    /**
     * Delete the balance entry with the given rowId
     * 
     * @param rowId id of entry to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEntry(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all entries in the database
     * 
     * @return Cursor over all entries
     */
    public Cursor fetchAllEntries() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DESCRIPTION,
                KEY_AMOUNT, KEY_CURRENCY, KEY_EVENT_DATE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of entry to retrieve
     * @return Cursor positioned to matching entry, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchEntry(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DESCRIPTION,
                    KEY_AMOUNT, KEY_CURRENCY, KEY_EVENT_DATE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the balance entry using the details provided. The entry to be updated is
     * specified using the rowId, and it is altered to use the description, amount and 
     * event date passed in
     * 
     * @param rowId id of entry to update
     * @param description value to set entry description to
     * @param amount value to set entry amount to
     * @param currency Currency to set for the entry
     * @param eventDate value to set entry eventDate to
     * @return true if the entry was successfully updated, false otherwise
     */
    public boolean updateEntry(long rowId, String description, double amount, String currency, Date eventDate) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, description);
        args.put(KEY_AMOUNT, amount);
        args.put(KEY_CURRENCY, currency);
        args.put(KEY_EVENT_DATE, eventDate.getTime() );
        
        boolean rtn = mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        
        if(rtn)
        {
        	Toast.makeText(mCtx, R.string.entry_saved, Toast.LENGTH_SHORT).show();
        }

        return rtn;
    }
}
