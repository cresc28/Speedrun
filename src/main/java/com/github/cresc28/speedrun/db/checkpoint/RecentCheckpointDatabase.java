package com.github.cresc28.speedrun.db.checkpoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RecentCheckpointデータベースの定義クラス。(CheckpointDatabaseは膨大になりうるので用意)
 */

public class RecentCheckpointDatabase {
    private static Connection con;
    private static final Logger LOGGER = Logger.getLogger("RecentCheckpointsDatabase");

    public static void initializeDatabase(){
        try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:plugins/speedrun/recentCheckpoints.sqlite");

            try(Statement stmt = con.createStatement()) {
                stmt.executeUpdate(
                                "CREATE TABLE IF NOT EXISTS recentCheckpoints (" +
                                        "uuid TEXT NOT NULL, " +
                                        "is_global INT NOT NULL, " + //globalRecentCp(全ワールドで最後に設定されたCPを指す)の場合は1に設定する。
                                        "world_uid TEXT NOT NULL, " + //world.getUID().toString()で取得する。
                                        "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, yaw DOUBLE NOT NULL, pitch DOUBLE NOT NULL, " +
                                        "PRIMARY KEY(uuid, world_uid))"
                );
            }
        } catch(Exception e){
            LOGGER.log(Level.SEVERE, "RecentCheckpointデータベース初期化中にエラーが発生しました", e);
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
                LOGGER.log(Level.SEVERE, "RecentCheckpointデータベースのクローズに失敗しました。", e);
            }
    }
}
