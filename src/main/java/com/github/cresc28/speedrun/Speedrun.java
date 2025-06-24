package com.github.cresc28.speedrun;

import com.github.cresc28.speedrun.command.CheckpointCommand;
import com.github.cresc28.speedrun.command.SpeedrunCommand;
import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.database.CheckpointDatabase;
import com.github.cresc28.speedrun.database.CourseDatabase;
import com.github.cresc28.speedrun.database.RecentCheckpointDatabase;
import com.github.cresc28.speedrun.listener.PlayerJoinQuitListener;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.manager.CourseDataManager;
import com.github.cresc28.speedrun.listener.PlayerInteractListener;
import com.github.cresc28.speedrun.listener.PlayerMoveListener;
import com.github.cresc28.speedrun.manager.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * サーバー起動時及び停止時の処理を記述するクラス。
 */

public final class Speedrun extends JavaPlugin implements Listener {
    CheckpointManager cpm = new CheckpointManager();


    @Override
    public void onEnable() {
        ConfigManager.init(); //設定の読み込みとSpeedrunディレクトリの作成。
        CourseDatabase.initializeDatabase();
        CheckpointDatabase.initializeDatabase();
        RecentCheckpointDatabase.initializeDatabase();

        CourseDataManager cdm = new CourseDataManager();
        TimerManager tm = new TimerManager(cdm);

        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(tm),this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(cpm),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(cpm),this);
        Bukkit.getLogger().info("Speedrunプラグインが起動しました。");

        tm.startTimer(this);
        getCommand("course").setExecutor(new SpeedrunCommand(cdm));
        getCommand("cp").setExecutor(new CheckpointCommand(cpm));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Location globalLoc = cpm.getGlobalRecentCpLocation(uuid);
            Location localLoc = cpm.getLocalRecentCpLocation(uuid);

            if (localLoc != null) cpm.saveRecentCp(uuid, false, localLoc);
            if (globalLoc != null) cpm.saveRecentCp(uuid, true, globalLoc);
        }

        CheckpointDatabase.closeConnection();
        RecentCheckpointDatabase.closeConnection();
        CourseDatabase.closeConnection();
    }
}
