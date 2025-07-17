package com.github.cresc28.speedrun.db.course;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViaPointRecordDatabase {
    private static Connection con;
    private static final Logger LOGGER = Logger.getLogger("ViaPointRecordDatabase");

    public static void initializeDatabase(){
        try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:plugins/speedrun/record.sqlite");

            try(Statement stmt = con.createStatement()){
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS record_via_point ( " +
                                "record_id INTEGER NOT NULL, " +
                                "via_point_name TEXT NOT NULL, " +
                                "tick INTEGER NOT NULL, " +
                                "PRIMARY KEY (record_id, via_point_name), " +
                                "FOREIGN KEY (record_id) REFERENCES record(record_id) ON DELETE CASCADE )"
                );
            }
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "viaPointRecordデータベース初期化中にエラーが発生しました", e);
        }
    }
}
