package com.github.cresc28.speedrun;

import com.github.cresc28.speedrun.command.SpeedrunCommand;
import com.github.cresc28.speedrun.data.CourseDataManager;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public final class Speedrun extends JavaPlugin implements Listener {
    private int tick = 0;
    private final Map<UUID, PlayerState> playerStates = new HashMap<>();

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
        getCommand("course").setExecutor(new SpeedrunCommand());
        CourseDataManager.load();
    }

    //計測開始
    private void startTimer(Player player, UUID uuid, Location loc, String courseName){
        PlayerState state = playerStates.computeIfAbsent(uuid, k -> new PlayerState());

        boolean started = state.start(courseName, tick, loc);
        if (started) {
            player.sendMessage("計測開始！");
        }
    }

    //計測終了
    private void endTimer(Player player, UUID uuid, Location loc, String courseName){
        PlayerState state = playerStates.computeIfAbsent(uuid, k -> new PlayerState());
        //TAを開始していることスタートの時のパルクール名との一致が条件
        if (state.isRunning() && courseName.equals(state.getCurrentCourse())) {
            state.stop();
            int record = tick - state.getStartTime();
            String timeString = Utils.formatTime(record);
            Bukkit.broadcastMessage(player.getName() + "さんが" + courseName + "を" + timeString + " (" + record + "tick)でクリア！");
        }

        //TAを開始したパルクール以外のパルクールのゴールを踏むとクリア表示のみを出す。
        // isOnEndはゴール連発防止のため。またスタートとは違い、別のゴールを連続して踏んでも重複表示は行わない。
        else if(!state.isOnEnd()) {
            Bukkit.broadcastMessage(player.getName() + "さんが" + courseName + "をクリア！");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation().getBlock().getLocation();
        String start = CourseDataManager.getStartMap().get(loc);
        String end = CourseDataManager.getEndMap().get(loc);
        PlayerState state = playerStates.computeIfAbsent(uuid, k -> new PlayerState());

        //プレイヤーの現在座標がいずれかのスタート地点と一致するならば処理を開始。
        if (start != null)  startTimer(player, uuid, loc, start);

        else if (end != null){
            endTimer(player, uuid, loc, end);
            state.setOnEnd(true);
        }

        else{
            state.setOnEnd(false);
        }

        state.updateLastStartLocation(loc);
    }
}
