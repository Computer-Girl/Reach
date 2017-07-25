package com.example.a85314.meshnetwork.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.a85314.meshnetwork.Database.*;
import com.example.a85314.meshnetwork.R;

import java.util.List;

/**
 * Custom view for displaying network data from Reach Mesh Network.
 * Relies on data from a database accessed by <code>NodeDatabaseHelper</code>.
 */
public class MeshNetDiagram extends ViewGroup {

    private final String HUB_MAC = "0013A200414970D1";


    private Context context;

    private NetGrid grid;
    GestureDetector detector;

    private int nodeNum;
    private List<Node> nodes;

    private Drawable nodeIcon;
    private Drawable nodeSelectedIcon;
    private Drawable hubIcon;
    private Drawable hubIconDisconnected;

    private Popup popup;

    private Paint linePaint;
    private Paint textPaint;

    private layout currentLayout;
//    private boolean[] selectedNodes;
    private Node selectedNode;
    private boolean hubConnected;

    private enum layout{
        TRIANGLE
    }

    /**
     * Create a new network diagram
     */
    public MeshNetDiagram(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        nodeIcon = context.getResources().getDrawable(R.drawable.node_icon, null);
        nodeSelectedIcon = context.getResources().getDrawable(R.drawable.node_icon_selected, null);
        hubIcon = context.getResources().getDrawable(R.drawable.hub_icon, null);
        hubIconDisconnected = context.getResources().getDrawable(R.drawable.hub_icon_disconnected, null);
        detector = new GestureDetector(MeshNetDiagram.this.getContext(), new mListener());
        popup = new Popup(800);
//        selectedNodes = new boolean[0];
        selectedNode = null;
        hubConnected = false;
        initializePaint();
        updateData();

    }

    /**
     * Updates diagram from data in database. Also updates custom node names in database.
     * Run this method each time the database is updated.
     */
    public  void updateData(){
        NodeDatabaseHelper db = new NodeDatabaseHelper(context);
        if (nodes != null){
            for (Node hereNode: nodes){
                db.updateNodeName(hereNode, hereNode.getName());
            }
        }
        nodeNum = db.getConnectedNodes().size();
        if(nodeNum <=3){
            currentLayout = layout.TRIANGLE;
        }
        else{
            // Place to implement layout for more than 3 nodes
            throw new Error("Number of nodes ("+ nodeNum +") not supported by NetGrid!");
        }
        if (nodes != null){
            nodes.clear();
        }
        nodes = db.getConnectedNodes();
        if (!nodes.contains(selectedNode)){
            selectedNode = null;
            popup.dismiss();
        } else{
            for (Node n: nodes){
                if (selectedNode.equals(n)){
                    selectedNode = n;
                }
            }
        }
        popup.setNode(selectedNode);
        postInvalidate();
        Log.i("MeshNetDiagram", " updateData run");
    }

    /**
     * Initialize paint objects
     */
    private void initializePaint(){
        linePaint = new Paint(0);
        linePaint.setColor(Color.DKGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10);
        textPaint = new Paint(0);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(30);
        textPaint.setFakeBoldText(true);

    }

    /**
     * Change whether the hub is currently connected to phone.
     * @param connected     true iff hub connected via Bluetooth to phone
     */
    public void setHubConnected(boolean connected){
        hubConnected = connected;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hubConnected){
            int thisLQI;
            for (int i = 0; i < nodeNum; i++){ //draw lines
                thisLQI = nodes.get(i).getLQI(HUB_MAC);
                if (thisLQI != 0){
                    linePaint.setStrokeWidth(thisLQI/10);
                    canvas.drawLine(grid.getHubCenter().x, grid.getHubCenter().y,
                            grid.getNodeCenter(i).x, grid.getNodeCenter(i).y, linePaint);
                }

                for (int j = 0; j < nodeNum; j++){
                    thisLQI = nodes.get(i).getLQI(nodes.get(j).getMac());
                    if (thisLQI !=0) {
                        linePaint.setStrokeWidth(thisLQI / 10);
                        canvas.drawLine(grid.getNodeCenter(i).x, grid.getNodeCenter(i).y,
                                grid.getNodeCenter(j).x, grid.getNodeCenter(j).y, linePaint);
                    }
                }
            }
            for (int i = 0; i < nodeNum; i++){ //draw icons
                if (selectedNode != null && selectedNode.equals(nodes.get(i))){
                    nodeSelectedIcon.setBounds(grid.getNodeRect(i));
                    nodeSelectedIcon.draw(canvas);
                } else {
                    nodeIcon.setBounds(grid.getNodeRect(i));
                    nodeIcon.draw(canvas);
                }
                textPaint.setColor(0xFF2b9187);
                canvas.drawText(nodes.get(i).getName(), grid.getNodeCenter(i).x,
                        (int) (grid.getNodeCenter(i).y+grid.nodeIconSize()*.65), textPaint);
            } //draw hub
            hubIcon.setBounds(grid.getHubRect());
            hubIcon.draw(canvas);
            textPaint.setColor(0xFF434343);
            canvas.drawText("Hub", grid.getHubCenter().x,
                    (int) (grid.getHubCenter().y-grid.nodeIconSize()*.56), textPaint);
        } else{
            hubIconDisconnected.setBounds(grid.getHubRect());
            hubIconDisconnected.draw(canvas);
            textPaint.setColor(0xFF434343);
            canvas.drawText("Hub (NOT CONNECTED)", grid.getHubCenter().x,
                    (int) (grid.getHubCenter().y-grid.nodeIconSize()*.56), textPaint);

        }

    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        grid = new NetGrid(w, h, nodeNum);
        popup = new Popup((int) (w*.85));
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event); //whether the gesture listener consumed the event
    }

    /**
     * Class for determining the positioning of the icons and lines in diagram
     */
    private class NetGrid{
        int w, h, n;

        /**
         * Create new <code>NetGrid</code> with new screen dimensions
         * @param width     width of view
         * @param height    height of view
         * @param numOfNodes    number of nodes to display
         */
        private NetGrid(int width, int height, int numOfNodes) {
            w = width;
            h = height;
            n = numOfNodes;
        }

        /**
         * Return the width/height of node icon.
         * @return  node icon width/height
         */
        private int nodeIconSize(){
            return w/5;
        }

        /**
         * Returns a <code>Rect</code> centered in view with correct dimensions for node icon.
         * @return  centered <code>Rect</code>
         */
        private Rect unalignedNodeRect(){
            return new Rect(-nodeIconSize()/2,-nodeIconSize()/2,nodeIconSize()/2,nodeIconSize()/2);
        }

        /**
         * Returns a <code>Rect</code> in the position where the hub icon should be placed
         * @return  positioned <code>Rect</code>
         */
        private Rect getHubRect(){
            Rect r = unalignedNodeRect();
            if (currentLayout == layout.TRIANGLE){
                r.offset(w/2, h/6);
                return r;
            } else{
                throw new Error("Number of nodes ("+n+") not supported by NetGrid!");
            }
        }

        /**
         * Returns a <code>Rect</code> in the position where a node icon should be placed
         * @param nodeNum       the node position number
         * @return              positioned <code>Rect</code>
         */
        private Rect getNodeRect(int nodeNum){
            Rect r;
            if (currentLayout == layout.TRIANGLE){
                switch (nodeNum){
                    case 0:
                        r = getHubRect();
                        r.offset(0, (h/3));
                        return r;
                    case 1:
                        r = getNodeRect(0);
                        r.offset(-h/5, h/4);
                        return r;
                    case 2:
                        r = getNodeRect(0);
                        r.offset(h/5, h/4);
                        return r;
                    default:
                        return new Rect();
                }
            } else{
                throw new Error("Number of nodes ("+n+") not supported by NetGrid!");
            }
        }

        /**
         * Return the center point of the hub icon.
         * @return      <code>Point</code> in the center of the hub icon
         */
        private Point getHubCenter(){
            return new Point((int) getHubRect().exactCenterX(), (int) getHubRect().exactCenterY());
        }

        /**
         * Return the center point of a node icon.
         * @param nodeNum       the node position number
         * @return              <code>Point</code> in the center of the node icon
         */
        private Point getNodeCenter(int nodeNum){
            return new Point((int) getNodeRect(nodeNum).exactCenterX(),
                    (int) getNodeRect(nodeNum).exactCenterY());
        }
    }

    /**
     * Popup window to display node information.
     */
    private class Popup{
        private Node n;
        private PopupWindow popup;
        private View parentView;
        private EditText name;
        private TextView rssi;
        private TextView temp;
        private TextView tempLabel;
        private TextView motion;
        private TextView light;
        private LinearLayout neighborView;
        private ImageButton closeButton;
        private ImageButton editButton;

        /**
         * Create a new Popup window. Initialized UI objects.
         */
        private Popup(int size){
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View.OnClickListener tempToggle = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences =
                            context.getSharedPreferences(context.getString(R.string.preference_file_key),
                                    Context.MODE_PRIVATE);
                    boolean currentValue =
                            sharedPreferences.getBoolean(context.getString(R.string.convert_to_fahrenheit),
                                    false);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (currentValue){
                        editor.putBoolean(context.getString(R.string.convert_to_fahrenheit), false);
                    }else{
                        editor.putBoolean(context.getString(R.string.convert_to_fahrenheit), true);
                    }
                    editor.apply();
                    update();
                }
            };
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.popup, null);
            popup = new PopupWindow(viewGroup, size, size, true);
            popup.setAnimationStyle(R.style.Animation);
            popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    selectedNode = null;
                    invalidate();
                }
            });
            name = (EditText) viewGroup.findViewById(R.id.nameEditText);
            name.setFocusable(false);
            name.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        name.setFocusableInTouchMode(false);
                    }
                }
            });
            name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE){
                        String input = name.getEditableText().toString();
                        name.setText(input.toCharArray(), 0, input.length());

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, 0);
                        name.setFocusableInTouchMode(false);
                        name.clearFocus();
                        n.setName(input);
                        updateData();
                        return true;
                    }
                    return false;
                }
            });
            rssi = (TextView) viewGroup.findViewById(R.id.rssiDisplay);
            temp = (TextView) viewGroup.findViewById(R.id.tempDisplay);
            temp.setOnClickListener(tempToggle);
            tempLabel = (TextView) viewGroup.findViewById(R.id.tempLabel);
            tempLabel.setOnClickListener(tempToggle);
            motion = (TextView) viewGroup.findViewById(R.id.motionDisplay);
            light = (TextView) viewGroup.findViewById(R.id.lightDisplay);
            closeButton = (ImageButton) viewGroup.findViewById(R.id.closeButton);
            closeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            editButton = (ImageButton) viewGroup.findViewById(R.id.editButton);
            editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    name.setFocusableInTouchMode(true);
                    name.requestFocus();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            neighborView = (LinearLayout) viewGroup.findViewById(R.id.neighborView);
            parentView = findViewById(R.id.Net);
        }

        /**
         * Sets the node whose data will be displayed
         * @param newNode       <code>Node</code> to display.
         */
        private void setNode(Node newNode){
            n = newNode;
            update();
        }

        /**
         * Display the popup.
         */
        private void show(){
            update();
            popup.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        }

        /**
         * Hide the popup.
         */
        private void dismiss(){
            popup.dismiss();
        }

        /**
         * Update the displayed data.
         */
        private void update(){
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(context.getString(R.string.preference_file_key),
                            Context.MODE_PRIVATE);
            if (n == null){
                return;
            }
            String nameString = n.getName();
            if (!name.isFocused()) {
                name.setText(nameString.toCharArray(), 0, nameString.length());
            }
            String tempString;
            double tempNum;
            if (sharedPreferences.getBoolean(context.getString(R.string.convert_to_fahrenheit),
                    false)){
                tempNum = n.getTemp()*1.8+32;
                tempNum = Math.round(tempNum*10.0)/10.0;
                tempString = Double.toString(tempNum)+" °F";
            } else {
                tempNum = n.getTemp();
                tempNum = Math.round(tempNum*10.0)/10.0;
                tempString = Double.toString(tempNum)+ " °C";
            }
            temp.setText(tempString.toCharArray(), 0, tempString.length());
            if (n.isMotion()){
                String yes = "Yes";
                motion.setText(yes.toCharArray(), 0, yes.length());
            } else {
                String no = "No";
                motion.setText(no.toCharArray(), 0, no.length());
            }
            String rssiString = "-"+Double.toString(n.getRssi())+" dBm";
            rssi.setText(rssiString.toCharArray(), 0, rssiString.length());
            String lightString = Integer.toString((int)((1-Math.round(n.getLight()*100.0)/100.0)*100))+"%";
            light.setText(lightString.toCharArray(), 0, lightString.length());

            neighborView.removeAllViews();
            for (String neighbor: n.getNeighborSet()) {
                TextView neighborText = new TextView(getContext());
                String name = null;
                if (neighbor.equals(HUB_MAC)){
                    name = "Hub";
                }
                else if (nodes.contains(new Node(neighbor))){
                    name = nodes.get(nodes.indexOf(new Node(neighbor))).getName();
                }
                if (name != null){
                    String nString = name+"    LQI: "+n.getLQI(neighbor)+"/255 packets";
                    neighborText.setText(nString.toCharArray(),0,nString.length());
                    neighborText.setTextColor(Color.WHITE);
                    neighborView.addView(neighborText);
                }
//                else{
//                    name = neighbor;
//                }

            }
        }


    }

    /**
     * Listener for selecting nodes.
     */
    private class mListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            e.getPointerCoords(0, coords);
            for (int i = 0; i<nodeNum; i++){
                if (grid.getNodeRect(i).contains((int) coords.x, (int) coords.y)){
                    selectedNode = nodes.get(i);
                    postInvalidate();
                    popup.setNode(nodes.get(i));
                    popup.show();
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
