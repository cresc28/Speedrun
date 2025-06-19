package com.github.cresc28.speedrun;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;


public final class Speedrun extends JavaPlugin implements Listener {

    int tick = 0;
    Map<String, String> startMap = SpeedrunCommand.getStartMap();
    Map<String, String> endMap = SpeedrunCommand.getEndMap();

    //登録データの読み取り
    public void load(){
        try (BufferedReader reader = new BufferedReader(new FileReader("speedrun_data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String key = parts[1];
                    String value = parts[2];
                    if (parts[0].equals("start")) {
                        SpeedrunCommand.putStartMap(key, value);
                    } else if (parts[0].equals("end")) {
                        SpeedrunCommand.putEndMap(key, value);
                    }
                }
            }
            System.out.println("Loading parkour data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getLogger().info("Speedrunプラグインが起動しました");
        new BukkitRunnable() {
            @Override
            public void run() {
                tick++;
            }
        }.runTaskTimer(this, 0, 1);
        getCommand("parkour").setExecutor(new SpeedrunCommand());
        load();
    }

    private Map<UUID, Integer> timeStart = new HashMap<>(); //スタート時のtickの値
    private Map<UUID, Boolean> started = new HashMap<>(); //スタートした状態か否か
    private Map<UUID, Boolean> onGoalLocation = new HashMap<>();//ゴールの上に経っているか否か
    private Map<UUID, String> parkourName = new HashMap<>(); //現在走っているパルクール名
    private Map<UUID, String> previousLocation = new HashMap<>(); //直前に踏んだスタートの位置

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        int x = (int) player.getLocation().getX();
        int y = (int) player.getLocation().getY();
        int z = (int) player.getLocation().getZ();
        String key = x + " " + y + " " + z + " " + player.getWorld().getName();

        if (startMap.containsKey(key)) {
            //直前に踏んだスタートと別のスタートを踏んだ時(前のスタートの位置(tmpLocation)と違う位置の場合)は作動させる(スタート連発防止)
            if (previousLocation.get(playerUUID) == null || !previousLocation.get(playerUUID).equals(key)) {
                parkourName.put(playerUUID,startMap.get(key));
                timeStart.put(playerUUID, tick);
                started.put(playerUUID, true);
                player.sendMessage("計測開始！");
            }
        }

        else if (endMap.containsKey(key)){
            //TAを開始していることスタートの時のパルクール名との一致が条件
            if (started.getOrDefault(playerUUID, true) && endMap.get(key).equals(parkourName.get(playerUUID))) {
                started.put(playerUUID, false);

                int record = tick - timeStart.get(playerUUID);
                int commas = (record%20) * 5;
                int seconds = (record/20) % 60;
                int minutes = (record/(60*20)) % 60;
                int hours = record/(60*60*20);
                String commasString = (commas < 10) ? "0" + commas: String.valueOf(commas);
                String secondsString = (seconds < 10) ? "0" + seconds: String.valueOf(seconds);
                String minutesString = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
                String hoursString = (hours < 10) ? "0" + hours : String.valueOf(hours);

                Bukkit.broadcastMessage(player.getName() + "さんが" + parkourName.get(playerUUID) + "を" +
                        hoursString + ":" + minutesString + ":" + secondsString + "." + commasString + "0 (" + record + "tick)でクリア！");
            }
            //TAを開始したパルクール以外のパルクールのゴールを踏むとクリア表示の実を出す。(ifはゴール連発防止のため)
            else if(!onGoalLocation.getOrDefault(playerUUID, false)) {
                Bukkit.broadcastMessage(player.getName() + "さんが" + endMap.get(key) + "をクリア！");
            }

            onGoalLocation.put(playerUUID,true);
        }

        else{
            onGoalLocation.put(playerUUID,false);
        }

        previousLocation.put(playerUUID,key);
    }

}
