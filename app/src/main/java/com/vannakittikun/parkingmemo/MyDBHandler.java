package com.vannakittikun.parkingmemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Rule on 12/8/2017.
 */

public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "parkingDB";

    public static final String TABLE_PARKING_HISTORY = "parking_history";

    public static final String ID = "_id";
    public static final String PARKING_ADDRESS = "address";
    public static final String PARKING_LAT = "latitude";
    public static final String PARKING_LNG = "longitude";
    public static final String PARKING_NOTE = "note";
    public static final String PARKING_IMAGE = "image";
    public static final String PARKING_PARKED = "parked";
    public static final String PARKING_ACCURACY = "accuracy";


    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_PARKING_HISTORY + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                PARKING_ADDRESS + " TEXT, " +
                PARKING_LAT + " TEXT, " +
                PARKING_LNG + " TEXT, " +
                PARKING_NOTE + " TEXT DEFAULT 'None', " +
                PARKING_IMAGE + " BLOB, " +
                PARKING_ACCURACY + " TEXT, " +
                PARKING_PARKED + " INTEGER DEFAULT 1 " +
                ");";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_HISTORY);
        onCreate(sqLiteDatabase);
    }

    public void addParking(double lat, double lng, float acc){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PARKING_LAT, lat);
        values.put(PARKING_LNG, lng);
        values.put(PARKING_ACCURACY, acc);
        db.insert(TABLE_PARKING_HISTORY, null, values);
    }

    public void updateParking(int id, double lat, double lng){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PARKING_LAT, lat);
        values.put(PARKING_LNG, lng);

        String[] args = new String[]{Integer.toString(id)};
        db.update(TABLE_PARKING_HISTORY, values, "_id=?", args);
    }

    public ArrayList<String> getAllParkingAddress(){
        ArrayList<String> addresses = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + PARKING_ADDRESS + " FROM " + TABLE_PARKING_HISTORY, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            addresses.add(c.getString(0));
            c.moveToNext();
        }

        return addresses;
    }

    public LatLng getParkingLatLng(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PARKING_HISTORY + " WHERE _id='" + id + "';", null);
        c.moveToFirst();
        if(c.getCount() > 0){
            LatLng latLng = new LatLng(c.getDouble(c.getColumnIndex(PARKING_LAT)), c.getDouble(c.getColumnIndex(PARKING_LNG)));
            return latLng;
        }
        return null;
    }

    public float getParkingAccuracy(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PARKING_HISTORY + " WHERE _id='" + id + "';", null);
        c.moveToFirst();
        if(c.getCount() > 0){
            return c.getFloat(c.getColumnIndex(PARKING_ACCURACY));
        }
        return 0;
    }

    public String getParkingNote(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PARKING_HISTORY + " WHERE _id='" + id + "';", null);
        c.moveToFirst();
        if(c.getCount() > 0){
            return c.getString(c.getColumnIndex(PARKING_NOTE));
        }
        return null;
    }


    public String getParkingAddress(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PARKING_HISTORY + " WHERE _id='" + id + "';", null);
        c.moveToFirst();
        if(c.getCount() > 0){
            return c.getString(c.getColumnIndex(PARKING_ADDRESS));
        }
        return null;
    }

    public Bitmap getParkingImage(int id){
        DbBitmapUtility dbBitmapUtility = new DbBitmapUtility();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PARKING_HISTORY + " WHERE _id='" + id + "';", null);
        c.moveToFirst();
        if(c.getCount() > 0){
            Bitmap bmp = dbBitmapUtility.getImage(c.getBlob(c.getColumnIndex(PARKING_IMAGE)));
            return bmp;
        }
        return null;
    }

    public int getCurrentParkingSession(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(_id) FROM " + TABLE_PARKING_HISTORY + ";", null);
        c.moveToFirst();
        if(c.getCount() > 0){
            return c.getInt(0);
        }
        return 0;
    }

    public void updateNote(int id, String note){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PARKING_NOTE, note);

        String[] args = new String[]{Integer.toString(id)};
        db.update(TABLE_PARKING_HISTORY, values, "_id=?", args);
    }

    public void updateImage(int id, Bitmap image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        DbBitmapUtility dbBitmapUtility = new DbBitmapUtility();

        values.put(PARKING_IMAGE, dbBitmapUtility.getBytes(image));

        String[] args = new String[]{Integer.toString(id)};
        db.update(TABLE_PARKING_HISTORY, values, "_id=?", args);
    }

    public void updateAddress(int id, String address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PARKING_ADDRESS, address);

        String[] args = new String[]{Integer.toString(id)};
        db.update(TABLE_PARKING_HISTORY, values, "_id=?", args);
    }

    public void unparkAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PARKING_PARKED, 0);

        db.update(TABLE_PARKING_HISTORY, values, null, null);
    }

    public boolean isStillParking(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + PARKING_PARKED + " FROM " + TABLE_PARKING_HISTORY + " WHERE " + PARKING_PARKED + "=1", null);
        c.moveToFirst();
        if(c.getCount() > 0){
            return true;
        }
        return false;
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }

}
