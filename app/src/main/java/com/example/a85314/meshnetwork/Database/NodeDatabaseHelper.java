package com.example.a85314.meshnetwork.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to access database of known Reach nodes, both those
 * currently connected and those disconnected.
 */
public class NodeDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "NodeData";

    private static final String TABLE_REMOTES = "RemoteNodes";

    private static final String KEY_MAC = "mac";
    private static final String KEY_NAME = "name";
    private static final String KEY_TEMP = "temp";
    private static final String KEY_MOTION = "motion";
    private static final String KEY_LIGHT = "light";
    private static final String KEY_RSSI = "rssi";
    private static final String KEY_BATTERY_LOW = "batt";
    private static final String KEY_NUM_NEIGHBORS = "numNeighbors";
    private static final String KEY_N_MACS = "neighborMacs";
    private static final String KEY_N_LQI = "neighborLqi";
    private static final String KEY_CONNECTED = "connected";

    /**
     * Create a new instance of the database helper.
     * @param context   Android context
     */
    public NodeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates a new node database
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NODES_TABLE = "CREATE TABLE " + TABLE_REMOTES + "("
                + KEY_MAC + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_TEMP + " REAL," + KEY_MOTION + " INTEGER,"
                + KEY_LIGHT + " REAL," + KEY_RSSI + " REAL,"
                + KEY_BATTERY_LOW + " INTEGER,"
                + KEY_NUM_NEIGHBORS + " INTEGER," + KEY_N_MACS + " TEXT,"
                + KEY_N_LQI + " TEXT," + KEY_CONNECTED + " INTEGER" + ")";
        db.execSQL(CREATE_NODES_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMOTES);

        // Create tables again
        onCreate(db);
    }

    public String getNodeName(String MAC){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT "+KEY_NAME+" FROM "+ TABLE_REMOTES+" WHERE "+KEY_MAC+" ='"+MAC+"'", null);


        if(res.getCount() > 0 ){
            res.moveToFirst();
            String name = res.getString(0);
            res.close();
            return name;
        }
        else{
            res.close();
            return MAC;
        }
    }

    /**
     * Add a node to the database. If node already exists, equivalent to
     * <code>updateNode</code>
     *
     * @param node  a new <code>Node</code> to add to database
     * @see #updateNode(Node)
     */
    public void addNode(Node node) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (getAllNodes().contains(node)){
            updateNodeKeepName(node);
            return;
        }

        ContentValues values = buildContentValues(node);

        // Inserting Row
        db.insert(TABLE_REMOTES, null, values);
        db.close(); // Closing database connection
    }

    /**
     * Returns <code>Node</code> in database with corresponding MAC address
     *
     * @param mac   MAC address of node to retrieve
     * @return      the <code>Node</code> from database
     */
    public  Node getNode(String mac) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_REMOTES, new String[]{KEY_MAC,
                        KEY_NAME, KEY_TEMP, KEY_MOTION, KEY_LIGHT, KEY_RSSI, KEY_BATTERY_LOW,
                        KEY_NUM_NEIGHBORS, KEY_N_MACS, KEY_N_LQI, KEY_CONNECTED}, KEY_MAC + "=?",
                new String[]{mac}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            Node node = new Node(cursor.getString(0));
            node.setName(cursor.getString(1));
            node.setTemp(Double.valueOf(cursor.getString(2)));
            node.setMotion(cursor.getString(3).equals("1"));
            node.setLight(Double.valueOf(cursor.getString(4)));
            node.setRssi(Double.valueOf(cursor.getString(5)));
            node.setBatteryLow(cursor.getString(6).equals("1"));
            String[] neighbors = cursor.getString(8).split(" ");
            String[] lqis = cursor.getString(9).split(" ");
            for (int i = 0; i < Integer.valueOf(cursor.getString(7)); i++) {
                node.addNeighbor(neighbors[i], Integer.valueOf(lqis[i]));
            }
            node.setConnected(cursor.getString(10).equals("1"));
            cursor.close();
            return node;
        }
        return null;
    }

    /**
     * Get all the <code>Node</code>s in the database
     * @return  <code>List</code> of all <code>Node</code>s in database
     */
    public  List<Node> getAllNodes() {
        List<Node> nodeList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_REMOTES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                nodeList.add(getNode(cursor.getString(0)));
            } while (cursor.moveToNext());
        }
        // return contact list
        cursor.close();
        return nodeList;
    }

    /**
     * Get all <code>Node</code>s in database that are currently connected.
     * @return  <code>List</code> of all <code>Node</code>s in database that are
     *          currently connected
     */
    public List<Node> getConnectedNodes(){
        List<Node> connectedNodeList = new ArrayList<>();
        for (Node n: getAllNodes()){
            if (n.isConnected()){
                connectedNodeList.add(n);
            }
        }
        return connectedNodeList;
    }

    /**
     * Updates <code>Node</code> in database with data in parameter
     * @param node      <code>Node</code> with data to update in database
     * @return          return value from database update function
     */
    public  int updateNode(Node node) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = buildContentValues(node);

        // updating row
        return db.update(TABLE_REMOTES, values, KEY_MAC + " = ?",
                new String[] { String.valueOf(node.getMac()) });
    }

    public int updateNodeKeepName(Node node){

        SQLiteDatabase db = this.getWritableDatabase();
        String oldName = getNode(node.getMac()).getName();
        node.setName(oldName);
        ContentValues values = buildContentValues(node);

        // updating row
        return db.update(TABLE_REMOTES, values, KEY_MAC + " = ?",
                new String[] { String.valueOf(node.getMac()) });
    }

    /**
     * Change the name of a <code>Node</code> in the database
     * @param n         <code>Node</code> for which name will be updated. No data
     *                  is copied from this object.
     * @param name      the new name of the <code>Node</code>
     * @return          return value of <code>updateNode()</code>
     */
    public int updateNodeName(Node n, String name){
        Node dbNode = getNode(n.getMac());
        dbNode.setName(name);
        return updateNode(dbNode);
    }

    /**
     * Remove a <code>Node</code> from the database
     * @param node    <code>Node</code> to remove from database
     */
    public void deleteNode(Node node) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMOTES, KEY_MAC + " = ?",
                new String[] { String.valueOf(node.getMac()) });
        db.close();
    }

    /**
     * Helper method for other database methods
     * @param node  <code>Node</code> for which to build the <code>ContentValues</code>
     * @return      <code>ContentValues</code> with data from parameter <code>Node</code>
     */
    private ContentValues buildContentValues(Node node){
        ContentValues values = new ContentValues();
        values.put(KEY_MAC, node.getMac());
        if (node.getName()== null){
            values.put(KEY_NAME, node.getMac());
        } else {
//            Log.i("databse", "builtContentValues assigned non-MAC name: "+node.getName());
            values.put(KEY_NAME, node.getName());
        }
        values.put(KEY_TEMP, node.getTemp());
        values.put(KEY_MOTION, node.isMotion());
        values.put(KEY_LIGHT, node.getLight());
        values.put(KEY_RSSI, node.getRssi());
        values.put(KEY_BATTERY_LOW, node.isBatteryLow());
        values.put(KEY_NUM_NEIGHBORS, node.getNeighborSet().size());
        String n_macs = "";
        for (String n : node.getNeighborSet()) {
            n_macs += (n + " ");
        }
        values.put(KEY_N_MACS, n_macs);
        String n_lqqi = "";
        for (String n : node.getNeighborSet()) {
            n_lqqi += (node.getLQI(n) + " ");
        }
        values.put(KEY_N_LQI, n_lqqi);
        values.put(KEY_CONNECTED, node.isConnected());
        return values;
    }
}
