package com.github.cresc28.speedrun.command;

import com.github.cresc28.speedrun.data.PointType;
import com.github.cresc28.speedrun.data.SpeedrunParameters;
import com.github.cresc28.speedrun.manager.CourseManager;
import com.github.cresc28.speedrun.utils.GameUtils;
import com.github.cresc28.speedrun.utils.MessageUtils;
import com.github.cresc28.speedrun.utils.TextUtils;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /courseコマンドの実装クラス。
 */
public class CourseCommand implements CommandExecutor, TabCompleter {
    private final CourseManager courseManager;

    public CourseCommand(SpeedrunParameters p) {
        courseManager = p.getCourseManager();
    }

    /**
     * TAB補完の実装。
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = Arrays.asList("add", "remove", "list", "tp");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) completions.add(option);
            }
        } else if (args.length == 2) {
            List<String> options = Arrays.asList("add", "remove", "list", "tp");

            if(!options.contains(args[0].toLowerCase())) return completions;

            for (PointType type : PointType.values()) {
                String typeName = type.name().toLowerCase();
                if (typeName.startsWith(args[1].toLowerCase())) {
                    completions.add(typeName); //タイプを補完
                }
            }

            if ("remove".equalsIgnoreCase(args[0]) || "tp".equalsIgnoreCase(args[0])) {
                TextUtils.completionFromCollection(courseManager.getAllCourseName(), args[1].toLowerCase(), completions); //存在するコースをすべて補完
            }

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("tp")) {
                PointType type = PointType.fromString(args[1]);
                TextUtils.completionFromCollection(courseManager.getAllCourseName(type), args[2].toLowerCase(), completions); //存在するコースをすべて補完
            }
        }

        return completions;
    }

    /**
     * コマンドの実装。
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) { //コンソールからの実行を禁止
            sender.sendMessage("このコマンドはサーバコンソールでは実行できません。");
            return true;
        }

        Player player = (Player) sender;
        String sub = args[0].toLowerCase();
        Location loc = GameUtils.getBlockLocation(player.getLocation());

        switch (sub) {
            case "list":
                Set<String> courses;
                if (args.length == 1) courses = courseManager.getAllCourseName();
                else if (args.length == 2) { //地点タイプが指定されているとき
                    PointType type = PointType.fromString(args[1]);
                    if (type == null) return false;
                    courses = courseManager.getAllCourseName(type);
                } else return false;

                MessageUtils.displayMap(courses, sender, "コース");
                return true;

            case "remove":
                boolean isRemoved;
                if (args.length == 2) isRemoved = courseManager.removeCourse(args[1]);
                else if (args.length == 3) { //地点タイプがしていされているとき
                    PointType type = PointType.fromString(args[1]);
                    if (type == null) return false;
                    isRemoved = courseManager.removeCourse(args[2], type);
                } else return false;

                MessageUtils.sendRemoveMessage(sender, isRemoved, args[1], "コース");
                return true;

            case "tp":
                Location locTeleport;
                if (args.length == 2) locTeleport = courseManager.getLocation(args[1], PointType.START);
                else if (args.length == 3) { //地点タイプが指定されているとき。
                    PointType type = PointType.fromString(args[1]);
                    if (type == null) return false;
                    locTeleport = courseManager.getLocation(args[2], type);
                } else return false;

                if (locTeleport != null) player.teleport(locTeleport);
                else sender.sendMessage("その名前のコースは登録されていません。");
                return true;

            case "add":
                String courseName;
                if (args.length == 3) {
                    if (TextUtils.containsChar(args[2], '.')) {
                        sender.sendMessage("コース名に.は使用できません。");
                        return true;
                    }
                    courseName = args[2];
                } else if (args.length == 4) { //中継地点名が含まれるとき
                    courseName = args[2] + "." + args[3];
                } else return false;

                PointType type = PointType.fromString(args[1]);
                if (type == null) return false;
                courseManager.registerCourse(type, courseName, loc);

                String typeDisplay = "";
                switch (type) {
                    case START:
                        typeDisplay = "スタート地点";
                        break;
                    case END:
                        typeDisplay = "ゴール地点";
                        break;
                    case VIA_POINT:
                        typeDisplay = "中継地点";
                        break;
                }

                if (args.length == 3)
                    sender.sendMessage(String.format("コース名:%s タイプ:%s 座標:%d %d %d", args[2], typeDisplay, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                else
                    sender.sendMessage(String.format("コース名: %s 中継地点名: %s 座標: %d %d %d", args[2], args[3], loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                return true;

            default: return false;
        }
    }
}