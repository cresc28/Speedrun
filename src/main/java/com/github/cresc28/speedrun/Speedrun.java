package com.github.cresc28.speedrun;

import com.github.cresc28.speedrun.command.CheckpointCommand;
import com.github.cresc28.speedrun.command.SpeedrunCommand;
import com.github.cresc28.speedrun.database.CheckpointsDatabase;
import com.github.cresc28.speedrun.database.RecentCheckpointsDatabase;
import com.github.cresc28.speedrun.listener.PlayerJoinQuitListener;
import com.github.cresc28.speedrun.manager.CourseDataManager;
import com.github.cresc28.speedrun.listener.PlayerInteractListener;
import com.github.cresc28.speedrun.listener.PlayerMoveListener;
import com.github.cresc28.speedrun.manager.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Speedrun extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        TimerManager timerManager = new TimerManager();

        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(timerManager),this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(),this);
        Bukkit.getLogger().info("Speedrunプラグインが起動しました。");

        timerManager.startTimer(this);
        getCommand("course").setExecutor(new SpeedrunCommand());
        getCommand("cp").setExecutor(new CheckpointCommand());
        CourseDataManager.load();

        CheckpointsDatabase.initializeDatabase();
        RecentCheckpointsDatabase.initializeDatabase();
    }

    @Override
    public void onDisable() {
        CheckpointsDatabase.closeConnection();
        RecentCheckpointsDatabase.closeConnection();
    }
}
