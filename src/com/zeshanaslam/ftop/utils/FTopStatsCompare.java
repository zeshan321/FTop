package com.zeshanaslam.ftop.utils;

import com.zeshanaslam.ftop.FTopStats;

import java.util.Comparator;

public class FTopStatsCompare implements Comparator<FTopStats> {
    @Override
    public int compare(FTopStats o1, FTopStats o2) {
        return Double.compare(o2.totalPoints, o1.totalPoints);
    }
}
