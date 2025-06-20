package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.data.CourseDataManager;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SpeedrunCommand implements CommandExecutor, TabCompleter {

    private Location getBlockLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private void sendRemoveMessage(CommandSender sender, boolean removed, String courseName) {
        if (removed) sender.sendMessage(courseName + "を削除しました。");
        else sender.sendMessage(ChatColor.RED + "その名前のコースは登録されていません。");
    }

    //登録されているコースの一覧表示
    private void displayCourse(Map<Location, String> map, CommandSender sender) {
        if (map.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "コースは登録されていません。");
            return;
        }

        Set<String> names = new HashSet<>(map.values());
        sender.sendMessage(names.toString());
    }

    //TAB補完
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!command.getName().equalsIgnoreCase("course")) return null;

        if (args.length == 1) {
            List<String> options = Arrays.asList("add", "remove", "list");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        else if(args.length == 2) {
            if ("add".equalsIgnoreCase(args[0]) || "remove".equalsIgnoreCase(args[0])) {
                List<String> options = Arrays.asList("start", "end");
                for (String option : options) {
                    if (option.startsWith(args[1].toLowerCase())) completions.add(option);
                }

                if ("remove".equalsIgnoreCase(args[0])) {
                    Utils.completionFromMap(CourseDataManager.getEndMap().values(), args[1].toLowerCase(), completions);
                }
            }
        }

        else if (args.length == 3) {
            if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    //startMapに登録されているアスレを表示
                    Utils.completionFromMap(CourseDataManager.getStartMap().values(), args[2].toLowerCase(), completions);
                } else if (args[1].equals("end")) {
                    Utils.completionFromMap(CourseDataManager.getEndMap().values(), args[2].toLowerCase(), completions);
                }
            }
        }
        return completions;
    }



    //コマンドの受け取り
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("course")) return false;

        if(args.length == 1 && args[0].equals("list")) {
            displayCourse(CourseDataManager.getStartMap(), sender);
            return true;
        }

        else if (args.length == 2 && args[0].equals("remove")) {
            boolean removedStart = CourseDataManager.removeCourse(CourseDataManager.getStartMap(), args[1]);
            boolean removedEnd = CourseDataManager.removeCourse(CourseDataManager.getEndMap(), args[1]);
            boolean removed = removedStart || removedEnd;
            if(removed) sendRemoveMessage(sender, removed, args[1]);
            else sender.sendMessage(ChatColor.RED + "その名前のコースは登録されていません。");
            return true;
        }

        if (args.length == 3) {
            if (args[0].equals("add")) {
                Player player = (Player) sender;
                Location loc = getBlockLocation(player.getLocation());

                if (args[1].equals("start")) {
                    CourseDataManager.registarCourse(loc,args[2],"start");
                    sender.sendMessage(String.format("スタート地点(%s) : %d %d %d", args[2], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    return true;
                }

                else if (args[1].equals("end")) {
                    CourseDataManager.registarCourse(loc,args[2],"end");
                    sender.sendMessage(String.format("ゴール地点(%s) : %d %d %d", args[2], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    return true;
                }

            }

            else if (args[0].equals("remove")) {
                if (args[1].equals("start")) {
                    boolean removed = CourseDataManager.removeCourse(CourseDataManager.getStartMap(), args[2]);
                    if(removed) sendRemoveMessage(sender, removed, args[2]);
                    else sender.sendMessage(ChatColor.RED + "その名前のコースは登録されていません。");
                    return true;
                }

                else if (args[1].equals("end")) {
                    boolean removed = CourseDataManager.removeCourse(CourseDataManager.getStartMap(), args[2]);
                    if(removed) sendRemoveMessage(sender, removed, args[2]);
                    else sender.sendMessage(ChatColor.RED + "その名前のコースは登録されていません。");
                    return true;
                }
            }
        }

        return false;
    }
}