package com.zeshanaslam.ftop.database.handlers;

import com.zeshanaslam.ftop.FTopStats;

import java.util.List;

public interface OnCalculateWorth {
    void onComplete(List<FTopStats> factionStats);
}
