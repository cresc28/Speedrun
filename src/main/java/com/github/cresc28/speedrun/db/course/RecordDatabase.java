package com.github.cresc28.speedrun.db.course;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recordデータベースの定義クラス。
 */
public class RecordDatabase {
    private static Connection con;
    private static final Logger LOGGER = Logger.getLogger("recordDatabase");

    public static void initializeDatabase(){
        try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:plugins/speedrun/record.sqlite");

            try(Statement stmt = con.createStatement()){
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS record (" +
                                "record_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "uuid TEXT NOT NULL, " +
                                "course_name TEXT NOT NULL, " +
                                "finish_tick INTEGER NOT NULL, " +
                                "record_at DATETIME NOT NULL)"
                );
            }
        } catch(Exception e){
            LOGGER.log(Level.SEVERE, "recordデータベース初期化中にエラーが発生しました", e);
        }
    }

    public static Connection getConnection(){
        if (con == null) LOGGER.warning("initializeDatabase()が呼ばれていません。");
        return con;
    }

    public static void closeConnection(){
        if(con != null){
            try{
                con.close();
            } catch(Exception e){
                LOGGER.log(Level.SEVERE, "Recordデータベースのクローズに失敗しました。", e);
            }
        }
    }
}
