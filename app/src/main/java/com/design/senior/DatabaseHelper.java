package com.design.senior;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Set params for SQLite tables
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "localDb";
    public static final long DBHEADERSIZE = 100;
    public static final long MAXDBSIZE = 10 * 1024 * 1024 ; // 10MB
    private static final String DETAIL_TABLE_NAME = "detailTable";
    private static final String SEARCH_TABLE_NAME = "searchTable";
    private static final String UPC_TABLE_NAME = "upcTable";
    private static final String DETAIL_COL0 = "fdcId";
    private static final String DETAIL_COL1 = "response";
    private static final String SEARCH_COL0 = "searchQuery";
    private static final String SEARCH_COL1 = "response";
    private static final String SEARCH_COL2 = "pageNumber";
    private static final String UPC_COL0 = "upc";
    private static final String UPC_COL1 = "name";

    // These files are used to determine the size of the database
    private static int mPageSize;
    File mDBFile;
    Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        mPageSize = getDBPageSize(db);
    }

    // Create tables if they don't already exist
    // Detail table has two columns: fdcId as integer, response as string
    // Search table has two columns: searchQuery as string, response as string
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDetailTable = "CREATE TABLE " + DETAIL_TABLE_NAME + " (" + DETAIL_COL0 + " INTEGER," + DETAIL_COL1 + " TEXT)";
        String createSearchTable = "CREATE TABLE " + SEARCH_TABLE_NAME + " (" + SEARCH_COL0 + " TEXT," + SEARCH_COL1 + " TEXT," + SEARCH_COL2 + " INTEGER)";
        String createUpcTable = "CREATE TABLE " + UPC_TABLE_NAME + " (" + UPC_COL0 + " TEXT," + UPC_COL1 + " TEXT)";

        db.execSQL(createDetailTable);
        db.execSQL(createSearchTable);
        db.execSQL(createUpcTable);
    }

    // Tables are dropped on upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + DETAIL_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SEARCH_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UPC_TABLE_NAME);
        onCreate(db);
    }

    // Checks if the fdcId is already located in the detail table
    public boolean detailCheckAlreadyExist(int fdcId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectString = "SELECT * FROM " + DETAIL_TABLE_NAME + " WHERE fdcId=?";
        Cursor cursor = db.rawQuery(selectString, new String[] {Integer.toString(fdcId)});

        if (cursor.getCount() > 0) {
            Log.d(TAG, "checkAlreadyExist: " + fdcId + " is already in " + DETAIL_TABLE_NAME);
            cursor.close();
            return true;
        }
        else {
            Log.d(TAG, "checkAlreadyExist: " + fdcId + " is not in " + DETAIL_TABLE_NAME);
            cursor.close();
            return false;
        }

    }

    // Adds fdcId and response as a row in the detail table
    public boolean detailAddData(int fdcId, String response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DETAIL_COL0, fdcId);
        contentValues.put(DETAIL_COL1, response);

        Log.d(TAG, "addData: Adding " + fdcId + " to " + DETAIL_TABLE_NAME);

        // If the database exceeds 10MB, all tables are cleared
        if (isDBFull()) {
            db.execSQL("DELETE FROM " + SEARCH_TABLE_NAME);
            db.execSQL("DELETE FROM " + DETAIL_TABLE_NAME);
            db.execSQL("DELETE FROM " + UPC_TABLE_NAME);
        }

        long result = db.insert(DETAIL_TABLE_NAME, null, contentValues);

        // If data is inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }

    // Fetch the detail response when given the fdcId
    public String detailGetData(int fdcId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectString = "SELECT * FROM " + DETAIL_TABLE_NAME + " WHERE fdcId=?";
        Cursor cursor = db.rawQuery(selectString, new String[] {Integer.toString(fdcId)});
        cursor.moveToFirst();
        String response = cursor.getString(1); // Column index 1 is for responses
        cursor.close();
        Log.d(TAG, "Fetched " + fdcId + " from " + DETAIL_TABLE_NAME);
        return response;
    }

    // Checks if the searchQuery is already located in the search table
    public boolean searchCheckAlreadyExist(String searchQuery, int pageNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectString = "SELECT * FROM " + SEARCH_TABLE_NAME + " WHERE searchQuery=? AND pageNumber=?";
        Cursor cursor = db.rawQuery(selectString, new String[] {searchQuery, String.valueOf(pageNumber)});

        if (cursor.getCount() > 0) {
            Log.d(TAG, "checkAlreadyExist: " + searchQuery + " is already in " + SEARCH_TABLE_NAME);
            cursor.close();
            return true;
        }
        else {
            Log.d(TAG, "checkAlreadyExist: " + searchQuery + " is not in " + SEARCH_TABLE_NAME);
            cursor.close();
            return false;
        }

    }

    // Adds searchQuery and response as a row in the search table
    public boolean searchAddData(String searchQuery, String response, int pageNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SEARCH_COL0, searchQuery);
        contentValues.put(SEARCH_COL1, response);
        contentValues.put(SEARCH_COL2, pageNumber);

        Log.d(TAG, "addData: Adding " + searchQuery + " to " + SEARCH_TABLE_NAME);

        // If the database exceeds 10MB, all tables are cleared
        if (isDBFull()) {
            db.execSQL("DELETE FROM " + SEARCH_TABLE_NAME);
            db.execSQL("DELETE FROM " + DETAIL_TABLE_NAME);
            db.execSQL("DELETE FROM " + UPC_TABLE_NAME);
        }

        long result = db.insert(SEARCH_TABLE_NAME, null, contentValues);

        // If data is inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }

    // Fetch the detail response when given the fdcId
    public String searchGetData(String searchQuery, int pageNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectString = "SELECT * FROM " + SEARCH_TABLE_NAME + " WHERE searchQuery=? AND pageNumber=?";
        Cursor cursor = db.rawQuery(selectString, new String[] {searchQuery, String.valueOf(pageNumber)});
        cursor.moveToFirst();
        String response = cursor.getString(1); // Column index 1 is for responses
        cursor.close();
        Log.d(TAG, "Fetched " + searchQuery + " from " + SEARCH_TABLE_NAME);
        return response;
    }

    // Checks if the UPC is already located in the UPC table
    public boolean upcCheckAlreadyExist(String gtinUpc) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectString = "SELECT * FROM " + UPC_TABLE_NAME + " WHERE upc=?";
        Cursor cursor = db.rawQuery(selectString, new String[] {gtinUpc});

        if (cursor.getCount() > 0) {
            Log.d(TAG, "checkAlreadyExist: " + gtinUpc + " is already in " + UPC_TABLE_NAME);
            cursor.close();
            return true;
        }
        else {
            Log.d(TAG, "checkAlreadyExist: " + gtinUpc + " is not in " + UPC_TABLE_NAME);
            cursor.close();
            return false;
        }

    }

    // Adds UPC and name as a row in the UPC table
    public boolean upcAddData(String gtinUpc, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UPC_COL0, gtinUpc);
        contentValues.put(UPC_COL1, name);

        Log.d(TAG, "addData: Adding " + gtinUpc + " to " + UPC_TABLE_NAME);

        // If the database exceeds 10MB, all tables are cleared
        if (isDBFull()) {
            db.execSQL("DELETE FROM " + SEARCH_TABLE_NAME);
            db.execSQL("DELETE FROM " + DETAIL_TABLE_NAME);
            db.execSQL("DELETE FROM " + UPC_TABLE_NAME);
        }

        long result = db.insert(UPC_TABLE_NAME, null, contentValues);

        // If data is inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }

    // Fetch the UPC name
    public String upcGetData(String gtinUpc) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectString = "SELECT * FROM " + UPC_TABLE_NAME + " WHERE upc=?";
        Cursor cursor = db.rawQuery(selectString, new String[] {gtinUpc});
        cursor.moveToFirst();
        String response = cursor.getString(1); // Column index 1 is for responses
        cursor.close();
        Log.d(TAG, "Fetched " + gtinUpc + " from " + UPC_TABLE_NAME);
        return response;
    }

    // The following functions are used to determine the size of the database
    private int getDBPageSize(SQLiteDatabase db) {
        int rv = 0;
        Cursor csr = db.rawQuery("PRAGMA page_size",null);
        if (csr.moveToFirst()) {
            rv = csr.getInt(csr.getColumnIndex("page_size"));
        }
        csr.close();
        return rv;
    }

    private long getDBFileSize() {
        if (mDBFile == null) {
            mDBFile = new File(mContext.getDatabasePath(DATABASE_NAME).toString());
        }
        return mDBFile.length();
    }

    public boolean isDBFull() {
        long fsz = getDBFileSize();
        return (fsz >= MAXDBSIZE - (mPageSize * 4) - DBHEADERSIZE);
    }

}
