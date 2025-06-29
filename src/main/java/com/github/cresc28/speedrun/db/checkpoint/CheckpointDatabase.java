package com.github.cresc28.speedrun.db.checkpoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLiteとの接続を行うクラス。
 */

public class CheckpointDatabase {
    private static Connection con;
    private static final Logger LOGGER = Logger.getLogger("CheckpointsDatabase");

    public static void initializeDatabase(){
        try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:plugins/speedrun/checkpoints.sqlite");

            try(Statement stmt = con.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS checkpoints (" +
                                "uuid TEXT NOT NULL, world_uid TEXT NOT NULL, cp_name TEXT NOT NULL, " +
                                "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, yaw DOUBLE NOT NULL, pitch DOUBLE NOT NULL, " +
                                "PRIMARY KEY(uuid, world_uid, cp_name))"
                );
            }
        } catch(Exception e){
            LOGGER.log(Level.SEVERE, "データベース初期化中にエラーが発生しました", e);
        }
    }

    public static Connection getConnection(){
        if (con == null) LOGGER.warning("initializeDatabase()が呼ばれていません。");
        return con;
    }

    public static void closeConnection(){
        if(con != null)
            try{
                con.close();
            } catch(Exception e){
                LOGGER.log(Level.SEVERE, "データベースのクローズに失敗しました。", e);
            }
    }
}
