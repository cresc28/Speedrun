package com.github.cresc28.speedrun.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * メッセージ表示に関するユーティリティクラス。
 */
public class MessageUtils {
    /**
     * あるコレクションの一覧を表示する。
     *
     * @param names 表示する要素の一覧
     * @param sender メッセージ送信先
     * @param subject 表示する対象の名前
     */
    public static void displayMap(Collection<String> names, CommandSender sender, String subject){
        if (names.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "登録済みの" + subject + "が存在しません。");
            return;
        }

        sender.sendMessage(String.join(", ", TextUtils.sortCollection(names)));
    }

    /**
     * 削除メッセージを送信する。
     *
     * @param sender メッセージ送信先
     * @param removed 削除が成功したか
     * @param name 削除対象の名前
     * @param subject 表示する対象の名前
     */
    public static void sendRemoveMessage(CommandSender sender, boolean removed, String name, String subject) {
        if (removed) sender.sendMessage(name + "を削除しました。");
        else sender.sendMessage(ChatColor.RED + "その名前の" + subject + "は登録されていません。");
    }
}
