package com.github.cresc28.speedrun.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 設定を管理するクラス。
 * また、Speedrunディレクトリの作成もここで行う。
 */

public class ConfigManager {
    private static final File FILE = new File("plugins/Speedrun/config.yml");
    private static final File TMP_FILE = new File("plugins/Speedrun/config.yml.tmp");
    private static FileConfiguration config;
    private static final Logger LOGGER = Logger.getLogger("ConfigManager");


    /**
     * config.ymlを読み込む。
     * config.ymlファイル及びSpeedrunディレクトリが存在しない場合は作成する。
     */
    public static void init() {
        File dir = FILE.getParentFile();

        if (!FILE.exists()) {
            boolean ignore1 = dir.mkdirs(); //Speedrunフォルダがなければ作成
            try {
                boolean ignore2 = FILE.createNewFile(); //config.ymlファイルを作成
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "config.ymlの作成に失敗しました。");
            }
        }

        config = YamlConfiguration.loadConfiguration(FILE);
        setDefault();
    }

    /**
     * configファイルにデフォルト値をセットする。
     * すでに設定がある場合は何もしない。
     */
    public static void setDefault(){
        if (!config.contains("isCrossWorldTpAllowed")) {
            config.set("isCrossWorldTpAllowed", false);
            saveConfig();
        }
    }

    /**
     * configファイルを保存する。
     */
    public static void saveConfig(){
        try{
            config.save(TMP_FILE);
            Files.move(TMP_FILE.toPath(), FILE.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch(IOException e){
            LOGGER.log(Level.SEVERE, "configの保存に失敗しました。");
        }
    }


    public static boolean isCrossWorldTpAllowed(){
        return config.getBoolean("isCrossWorldTpAllowed", false);
    }

    public static void setCrossWorldTpAllowed(boolean allowed){
        config.set("isCrossWorldTpAllowed",allowed);
        saveConfig();
    }
}
