package com.github.cresc28.speedrun;

import com.github.cresc28.speedrun.command.CheckpointCommand;
import com.github.cresc28.speedrun.command.CourseCommand;
import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.db.CheckpointDatabase;
import com.github.cresc28.speedrun.db.CourseDatabase;
import com.github.cresc28.speedrun.db.RecentCheckpointDatabase;
import com.github.cresc28.speedrun.core.listener.PlayerJoinQuitListener;
import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.core.manager.CourseDataManager;
import com.github.cresc28.speedrun.core.listener.PlayerInteractListener;
import com.github.cresc28.speedrun.core.listener.PlayerMoveListener;
import com.github.cresc28.speedrun.core.manager.TimerManager;
import com.github.cresc28.speedrun.config.message.CourseMessage;
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
        CourseMessage.init();
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
        getCommand("course").setExecutor(new CourseCommand(cdm));
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
