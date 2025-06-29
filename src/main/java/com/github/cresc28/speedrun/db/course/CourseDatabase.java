package com.github.cresc28.speedrun.db.course;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLiteとの連携を行うクラス
 */

public class CourseDatabase {
    private static Connection con;
    private static final Logger LOGGER = Logger.getLogger("CourseDatabase");

    public static void initializeDatabase(){
        try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:plugins/speedrun/courseData.sqlite");

            try(Statement stmt = con.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS courseData (" +
                                "type INT NOT NULL, " + //コースのタイプ
                                "course_name TEXT NOT NULL, " +
                                "world_uid TEXT NOT NULL, " +
                                "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, " +
                                "PRIMARY KEY(type, world_uid, x, y, z))"
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
