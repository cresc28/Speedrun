package com.github.cresc28.speedrun;

import com.github.cresc28.speedrun.command.SpeedrunCommand;
import com.github.cresc28.speedrun.data.ParkourDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;


public final class Speedrun extends JavaPlugin implements Listener {

    private int tick = 0;
    private Map<Location, String> startMap = new HashMap<>();
    private Map<Location, String> endMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getLogger().info("Speedrunプラグインが起動しました。");
        new BukkitRunnable() {
            @Override
            public void run() {
                tick++;
            }
        }.runTaskTimer(this, 0, 1);
        getCommand("parkour").setExecutor(new SpeedrunCommand());
        ParkourDataManager.load();
        startMap = ParkourDataManager.getStartMap();
        endMap = ParkourDataManager.getEndMap();
    }

    private Map<UUID, Integer> timeStart = new HashMap<>(); //スタート時のtickの値
    private Map<UUID, Boolean> started = new HashMap<>(); //スタートした状態か否か
    private Map<UUID, Boolean> onGoalLocation = new HashMap<>();//ゴールの上に経っているか否か
    private Map<UUID, String> parkourName = new HashMap<>(); //現在走っているパルクール名
    private Map<UUID, Location> previousLocation = new HashMap<>(); //直前に踏んだスタートの位置

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Location loc = player.getLocation().getBlock().getLocation();

        if (startMap.containsKey(loc)) {
            //直前に踏んだスタートと別のスタートを踏んだ時(前のスタートの位置(tmpLocation)と違う位置の場合)は作動させる(スタート連発防止)
            if (previousLocation.get(playerUUID) == null || !previousLocation.get(playerUUID).equals(loc)) {
                parkourName.put(playerUUID,startMap.get(loc));
                timeStart.put(playerUUID, tick);
                started.put(playerUUID, true);

                player.sendMessage("計測開始！");
            }
        }

        else if (endMap.containsKey(loc)){
            //TAを開始していることスタートの時のパルクール名との一致が条件
            if (started.getOrDefault(playerUUID, true) && endMap.get(loc).equals(parkourName.get(playerUUID))) {
                started.put(playerUUID, false);

                int record = tick - timeStart.get(playerUUID);
                String timeString = formatTime(record);

                Bukkit.broadcastMessage(player.getName() + "さんが" + parkourName.get(playerUUID) + "を" + timeString + " (" + record + "tick)でクリア！");
            }
            //TAを開始したパルクール以外のパルクールのゴールを踏むとクリア表示の実を出す。(ifはゴール連発防止のため)
            else if(!onGoalLocation.getOrDefault(playerUUID, false)) {
                Bukkit.broadcastMessage(player.getName() + "さんが" + endMap.get(loc) + "をクリア！");
            }

            onGoalLocation.put(playerUUID,true);
        }

        else{
            onGoalLocation.put(playerUUID,false);
        }

        previousLocation.put(playerUUID,loc);
    }

    private String formatTime(int ticks){
        int commas = (ticks % 20) * 5;
        int seconds = (ticks / 20) % 60;
        int minutes = (ticks / (60 * 20)) % 60;
        int hours = ticks / (60 * 60 * 20);

        return String.format("%02d:%02d:%02d.%02d0", hours, minutes, seconds, commas);
    }

}
