package com.github.cresc28.speedrun;

import com.github.cresc28.speedrun.command.CheckpointCommand;
import com.github.cresc28.speedrun.command.CourseCommand;
import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.core.listener.*;
import com.github.cresc28.speedrun.db.checkpoint.CheckpointDatabase;
import com.github.cresc28.speedrun.db.course.CourseDatabase;
import com.github.cresc28.speedrun.db.checkpoint.RecentCheckpointDatabase;
import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.core.manager.CourseManager;
import com.github.cresc28.speedrun.core.manager.TimerManager;
import com.github.cresc28.speedrun.config.message.CourseMessage;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * サーバー起動時及び停止時の処理を記述するクラス。
 */

public final class Speedrun extends JavaPlugin implements Listener {
    CheckpointManager cpManager = new CheckpointManager();

    @Override
    public void onEnable() {
        ConfigManager.init(); //設定の読み込みとSpeedrunディレクトリの作成。
        CourseMessage.init();
        CourseDatabase.initializeDatabase();
        CheckpointDatabase.initializeDatabase();
        RecentCheckpointDatabase.initializeDatabase();

        CourseManager courseManager = new CourseManager();
        TimerManager timeManager = new TimerManager(courseManager, cpManager);

        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(timeManager),this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(cpManager),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(cpManager),this);
        Bukkit.getPluginManager().registerEvents(new InventoryActionListener(cpManager, this),this);
        Bukkit.getPluginManager().registerEvents(new SignChangeListener(),this);
        Bukkit.getLogger().info("Speedrunプラグインが起動しました。");

        timeManager.startTimer(this);
        getCommand("course").setExecutor(new CourseCommand(courseManager));
        getCommand("cp").setExecutor(new CheckpointCommand(cpManager));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Location globalLoc = cpManager.getGlobalRecentCpLocation(uuid);
            Location localLoc = cpManager.getLocalRecentCpLocation(uuid);

            if (localLoc != null) cpManager.saveRecentCp(uuid, false, localLoc);
            if (globalLoc != null) cpManager.saveRecentCp(uuid, true, globalLoc);
        }

        CheckpointDatabase.closeConnection();
        RecentCheckpointDatabase.closeConnection();
        CourseDatabase.closeConnection();
    }
}
