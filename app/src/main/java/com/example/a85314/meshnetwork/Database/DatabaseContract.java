package com.example.a85314.meshnetwork.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a85314 on 6/22/2017.
 */

public class DatabaseContract extends SQLiteOpenHelper
{


    //DatabaseContract version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Reach.db";

    private static final String DB_PATH = "data/data/com.example.a85314.meshnetwork/databases/Reach.db";


    public static final String SENSOR_DATA_TABLE_NAME ="SensorData";
    public static final String SENSOR_DATA_COLUMN_ID = "SensorDataID";
    public static final String NODE_COLUMN_NAME = "Nodes";



    //Constructor for the DatabaseContract activity
    public DatabaseContract( Context context , String name, SQLiteDatabase.CursorFactory factory, int version) {

        super( context, name, factory, version);

    }


    //onCreate method creates table in the database if not already
    @Override
    public void onCreate(SQLiteDatabase db)
    {

        db.execSQL("CREATE TABLE "+SENSOR_DATA_TABLE_NAME+" ("+SENSOR_DATA_COLUMN_ID+" INTEGER PRIMARY KEY, "+NODE_COLUMN_NAME+
                " STRING)");

    }



    //upgrades the database if need be for newer version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

        db.execSQL("DROP TABLE IF EXISTS " + SENSOR_DATA_TABLE_NAME);
        onCreate(db);
    }

    public void Delete(){
        SQLiteDatabase db =this.getWritableDatabase();
        db.execSQL("delete from "+ SENSOR_DATA_TABLE_NAME);
    }


    public boolean checkExist(String nodeNumber)
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM "+ SENSOR_DATA_TABLE_NAME+" WHERE "+NODE_COLUMN_NAME+" = '"+nodeNumber+"'";

        Cursor res = db.rawQuery(query, null);

        /**for testing, for final product test that it equals 1**/
        if (res.getCount() > 0){
            return true;
        }else{
            return false;
        }


    }

    public String updateSensorData(String node_number)
    {

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + SENSOR_DATA_TABLE_NAME + " WHERE " + NODE_COLUMN_NAME + " ='" + node_number + "'";
        Cursor res = db.rawQuery(query, null);



       if (res.getCount() == 0){

           res.close();
           ContentValues content = new ContentValues();
           content.put(NODE_COLUMN_NAME, node_number);
           db.insert(SENSOR_DATA_TABLE_NAME, null, content);
           return "res == 0";

       }else{
           return "res == 1";
       }

    }

    public Cursor getID(String node){

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery( "SELECT "+SENSOR_DATA_COLUMN_ID+" FROM " + SENSOR_DATA_TABLE_NAME+" WHERE "+NODE_COLUMN_NAME+
                " ='"+node+"'", null );


    }



}



