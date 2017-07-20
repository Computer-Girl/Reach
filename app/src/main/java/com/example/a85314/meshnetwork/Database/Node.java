package com.example.a85314.meshnetwork.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class representing a Reach node.
 */
public class Node {
    private String mac, name;
    private double temp, light, rssi;
    private boolean motion, connected;
    private Map<String, Integer> neighborMACtoLQI;

    /**
     * Create a new Reach node.
     * @param mac       MAC address of node. This is unique for each node and cannot be changed.
     */
    public Node(String mac) {
        this.mac = mac;
        this.connected = false;
        neighborMACtoLQI = new HashMap<>();
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public boolean isMotion() {
        return motion;
    }

    public void setMotion(boolean motion) {
        this.motion = motion;
    }

    public void addNeighbor(String neighborMAC, int lqi){
        neighborMACtoLQI.put(neighborMAC, lqi);
    }

    public Set<String> getNeighborSet(){
        return neighborMACtoLQI.keySet();
    }

    public int getLQI(String neighborMAC){
        if (!neighborMACtoLQI.containsKey(neighborMAC)){
            return 0;
        }
        return neighborMACtoLQI.get(neighborMAC);
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return mac != null ? mac.equals(node.mac) : node.mac == null;
    }
}
