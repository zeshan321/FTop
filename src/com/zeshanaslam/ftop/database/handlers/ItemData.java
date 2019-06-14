package com.zeshanaslam.ftop.database.handlers;

public class ItemData {

    public String materialType;
    public int amount;
    public int data;

    public ItemData(String materialType, int amount, byte data) {
        this.materialType = materialType;
        this.amount = amount;
        this.data = data;
    }
}
