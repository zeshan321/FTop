package com.zeshanaslam.ftop.config.challenges;

public class Objective {

    public String type;
    public int amountMin;
    public int amountMax;
    public int amount;
    public String text;

    public Objective(String type, int amountMin, int amountMax, String text) {
        this.type = type;
        this.amountMin = amountMin;
        this.amountMax = amountMax;
        this.text = text;
    }
}
