package com.github.cresc28.speedrun;

import com.github.cresc28.speedrun.command.CheckpointCommand;
import com.github.cresc28.speedrun.command.CourseCommand;
import com.github.cresc28.speedrun.command.RecordCommand;
import com.github.cresc28.speedrun.command.TopCommand;
import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.data.SpeedrunParameters;
import com.github.cresc28.speedrun.db.record.RecordDao;
import com.github.cresc28.speedrun.db.record.RecordDatabase;
import com.github.cresc28.speedrun.db.record.ViaPointRecordDatabase;
import com.github.cresc28.speedrun.event.*;
import com.github.cresc28.speedrun.db.checkpoint.CheckpointDatabase;
import com.github.cresc28.speedrun.db.course.*;
import com.github.cresc28.speedrun.db.checkpoint.RecentCheckpointDatabase;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.manager.CourseManager;
import com.github.cresc28.speedrun.manager.TimerManager;
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
    private final CheckpointManager cpManager = new CheckpointManager();

    @Override
    public void onEnable() {
        initConfigs(); //設定の読み込みとSpeedrunディレクトリの作成。
        initDatabases();

        RecordDao recordDao = new RecordDao();
        CourseDao courseDao = new CourseDao();
        CourseManager courseManager = new CourseManager(courseDao);
        TimerManager timerManager = new TimerManager(courseManager, cpManager, recordDao);
        SpeedrunParameters parameters = new SpeedrunParameters(courseManager, cpManager, recordDao);

        registerEvents(timerManager, parameters);
        registerCommands(parameters);

        timerManager.startTimer(this);

        Bukkit.getLogger().info("Speedrunプラグインが起動しました。");
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
        CourseDatabase.closeConnection();
        RecordDatabase.closeConnection();
    }

    private void initConfigs(){
        ConfigManager.init(); //設定の読み込みとSpeedrunディレクトリの作成。
        CourseMessage.init();
    }

    private void initDatabases(){
        CourseDatabase.initializeDatabase();
        CheckpointDatabase.initializeDatabase();
        RecentCheckpointDatabase.initializeDatabase();
        RecordDatabase.initializeDatabase();
        ViaPointRecordDatabase.initializeDatabase();
    }

    private void registerEvents(TimerManager timerManager, SpeedrunParameters parameters){
        Listener[] listeners = {
                new PlayerMoveListener(timerManager),
                new PlayerInteractListener(parameters),
                new PlayerJoinQuitListener(parameters),
                new InventoryActionListener(parameters, this),
                new SignChangeListener()
        };
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    private void registerCommands(SpeedrunParameters parameters) {
        getCommand("course").setExecutor(new CourseCommand(parameters));
        getCommand("cp").setExecutor(new CheckpointCommand(parameters));
        getCommand("top").setExecutor(new TopCommand(parameters));
        getCommand("record").setExecutor(new RecordCommand(parameters));
    }
}
