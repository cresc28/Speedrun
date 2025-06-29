package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.data.CourseType;
import com.github.cresc28.speedrun.core.manager.CourseManager;
import com.github.cresc28.speedrun.utils.MessageUtils;
import com.github.cresc28.speedrun.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /courseコマンドの実装クラス。
 */
public class CourseCommand implements CommandExecutor, TabCompleter {
    private final CourseManager cm;

    public CourseCommand(CourseManager cm) {
        this.cm = cm;
    }

    /**
     * Tab補完の処理を行う。
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = Arrays.asList("add", "remove", "list", "tp");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        }

        else if(args.length == 2) {
            List<String> options = Arrays.asList("add", "remove", "list", "tp");
            String str = args[0].toLowerCase();
            if (options.contains(str)) {
                for (CourseType type : CourseType.values()) {
                    String typeName = type.name().toLowerCase();
                    if (typeName.startsWith(args[1].toLowerCase())) {
                        completions.add(typeName);
                    }
                }

                if ("remove".equalsIgnoreCase(args[0]) || "tp".equalsIgnoreCase(args[0])) {
                    Utils.completionFromMap(cm.getAllCourseName(), args[1].toLowerCase(), completions);
                }
            }
        }

        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("tp")) {
                CourseType type = CourseType.fromString(args[1]);
                Utils.completionFromMap(cm.getAllCourseName(type), args[2].toLowerCase(), completions);
            }
        }

        return completions;
    }

    /**
     * コマンドの実行処理を行う。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Location loc = Utils.getBlockLocation(player.getLocation());

        if(args.length == 1 && args[0].equalsIgnoreCase("list")) {
            MessageUtils.displayMap(cm.getAllCourseName(), sender, "コース");
            return true;
        }

        else if (args.length == 2) {
            if(args[0].equalsIgnoreCase("remove")) {
                MessageUtils.sendRemoveMessage(sender, cm.removeCourse(args[1]), args[1], "コース");
                return true;
            }

            else if(args[0].equalsIgnoreCase("list")){
                CourseType type = CourseType.fromString(args[1]);
                MessageUtils.displayMap(cm.getAllCourseName(type), sender, "コース");
                return true;
            }

            else if(args[0].equalsIgnoreCase("tp")){
                Location locTeleport = cm.getLocation(CourseType.START, args[1]);
                if(locTeleport != null) player.teleport(locTeleport);
                else player.sendMessage("その名前のコースは登録されていません。");
                return true;
            }


        }

        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {

                if(Utils.containsChar(args[2], '.')){
                    sender.sendMessage("コース名に.は使用できません。");
                    return true;
                }

                if (args[1].equalsIgnoreCase("start")) {
                    cm.registerCourse(CourseType.START, args[2], loc);
                    sender.sendMessage(String.format("%s（スタート） : %d %d %d", args[2], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    return true;
                }

                if (args[1].equalsIgnoreCase("via_point")) {
                    cm.registerCourse(CourseType.VIA_POINT, args[2], loc);
                    sender.sendMessage(String.format("%s（中間） : %d %d %d", args[2], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    return true;
                }

                else if (args[1].equalsIgnoreCase("end")) {
                    cm.registerCourse(CourseType.END, args[2], loc);
                    sender.sendMessage(String.format("%s（ゴール） : %d %d %d", args[2], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                    return true;
                }

            }

            else if (args[0].equalsIgnoreCase("remove")) {
                boolean removed = cm.removeCourse(args[2], CourseType.fromString(args[1]));
                MessageUtils.sendRemoveMessage(sender, removed, args[2], "コース");
                return true;
            }

            else if (args[0].equalsIgnoreCase("tp")) {
                Location locTeleport = cm.getLocation(CourseType.fromString(args[1]), args[2]);
                if(locTeleport != null) player.teleport(locTeleport);
                else player.sendMessage("その名前のコースは登録されていません。");
                return true;
            }
        }

        else if (args.length == 4 && args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("via_point")) {
            cm.registerCourse(CourseType.VIA_POINT, args[2] + "." + args[3], loc);
            sender.sendMessage(String.format("%s（中間, 地点名: %s） : %d %d %d", args[2], args[3], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            return true;
        }

        return false;
    }
}