package com.github.cresc28.speedrun;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SpeedrunCommand implements CommandExecutor, TabCompleter {
    static Map<String, String> startMap = new HashMap<>();
    static Map<String, String> endMap = new HashMap<>();

    public void save() {
        try (FileWriter writer = new FileWriter("speedrun_data.txt")) {
            // startMap の内容を保存
            for (Map.Entry<String, String> entry : startMap.entrySet()) {
                writer.write("start," + entry.getKey() + "," + entry.getValue() + "\n");
            }
            // endMap の内容を保存
            for (Map.Entry<String, String> entry : endMap.entrySet()) {
                writer.write("end," + entry.getKey() + "," + entry.getValue() + "\n");
            }
            System.out.println("Saving parkour data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //登録されているparkourの削除
    public void removeParkour(Map<String, String> map, CommandSender sender, String parkourName) {
        List<String> keysToRemove = new ArrayList<>();
        for (String key : map.keySet()) {
            if (map.get(key).equals(parkourName)) keysToRemove.add(key);
        }

        if (keysToRemove.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "その名前のパルクールは登録されていません。");
        } else {
            sender.sendMessage(map.get(keysToRemove.get(0)) + "を削除しました。");
        }

        for (String keyToRemove : keysToRemove) {
            map.remove(keyToRemove);
        }
        save();
    }

    //登録されているparkourの一覧表示
    public void displayParkour(Map<String, String> map, CommandSender sender) {
        if (map.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No parkour registered");
            return;
        }

        Set<String> startSet = new HashSet<>(map.values());
        String[] startList = startSet.toArray(new String[0]);
        sender.sendMessage(Arrays.toString(startList));
    }

    //TAB補完
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("parkour")) {
            if (args.length == 1) {
                if ("add".startsWith(args[0].toLowerCase())) {
                    completions.add("add");
                }
                if ("remove".startsWith(args[0].toLowerCase())) {
                    completions.add("remove");
                }
                if ("list".startsWith(args[0].toLowerCase())) {
                    completions.add("list");
                }

            } else if (args.length == 2 && (args[0].equals("add") || args[0].equals("remove") || args[0].equals("list"))) {
                if ("start".startsWith(args[1].toLowerCase())) {
                    completions.add("start");
                }
                if ("end".startsWith(args[1].toLowerCase())) {
                    completions.add("end");
                }
            } else if (args.length == 3) {
                if (args[0].equals("remove")) {

                    if (args[1].equals("start")) {
                        //startMapに登録されているアスレを表示
                        Set<String> startSet = new HashSet<>(startMap.values());
                        for (String value : startSet) {
                            if (value.toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(value);
                            }
                        }
                    } else if (args[1].equals("end")) {
                        //endMapに登録されているアスレを表示
                        Set<String> endSet = new HashSet<>(endMap.values());
                        for (String value : endSet) {
                            if (value.toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(value);
                            }
                        }
                    }
                } else return completions;
            }
            return completions;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("parkour")) {
            if (args.length == 3) {
                if (args[0].equals("add")) {
                    Player player = (Player) sender;
                    Location playerLocation = player.getLocation();
                    World world = player.getWorld();
                    int x = (int) playerLocation.getX();
                    int y = (int) playerLocation.getY();
                    int z = (int) playerLocation.getZ();
                    String key = x + " " + y + " " + z + " " + world.getName();

                    if (args[1].equals("start")) {
                        startMap.put(key, args[2]);
                        save();
                    } else if (args[1].equals("end")) {
                        endMap.put(key, args[2]);
                        save();
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /parkour [add/remove] [start/end] [name]");
                    }
                } else if (args[0].equals("remove")) {

                    if (args[1].equals("start")) {
                        removeParkour(startMap, sender, args[2]);
                    } else if (args[1].equals("end")) {
                        removeParkour(endMap, sender, args[2]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /parkour [add/remove] [start/end] [name]");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /parkour [add/remove/list] [start/end] [name]");
                }
            } else if (args.length == 2) {
                if (args[0].equals("list")) {
                    if (args[1].equals("start")) {
                        displayParkour(startMap, sender);
                    } else if (args[1].equals("end")) {
                        displayParkour(endMap, sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /parkour list [start/end]");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /parkour [add/remove/list] [start/end] [name]");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /parkour [add/remove/list] [start/end] [name]");
            }

            return true;
        }
        return false;
    }

    public static Map<String, String> getStartMap() {
        return startMap;
    }

    public static Map<String, String> getEndMap() {
        return endMap;
    }

    public static void putStartMap(String key, String value) {
        startMap.put(key, value);
    }

    public static void putEndMap(String key, String value) {
        endMap.put(key, value);
    }
}
