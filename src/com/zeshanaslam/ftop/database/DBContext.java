package com.zeshanaslam.ftop.database;

import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.database.handlers.BlockTable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    private final Main main;
    private Connection connection;

    public DBContext(Main main) throws SQLException, IOException {
        this.main = main;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        File file = new File("plugins/FTop/data.db");
        if (!file.exists()) {
            this.connection = DriverManager.getConnection("jdbc:sqlite:plugins/FTop/data.db");
            getBlockTable().create();
        } else {
            this.connection = DriverManager.getConnection("jdbc:sqlite:plugins/FTop/data.db");
        }

        getBlockTable().vacuum();
    }

    public Main getInstance() {
        return main;
    }

    public Connection getConnection() {
        return connection;
    }

    public BlockTable getBlockTable() {
        return new BlockTable(this);
    }
}
