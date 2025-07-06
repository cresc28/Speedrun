package com.github.cresc28.speedrun.core.listener;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * インベントリのクリック時、閉じた時の処理。
 */

public class InventoryActionListener implements Listener {
    private final CheckpointManager cpManager;
    private final Map<UUID, Boolean> deleteModeMap = new HashMap<>();
    private final JavaPlugin plugin;

    public InventoryActionListener(CheckpointManager cpManager, JavaPlugin plugin) {
        this.cpManager = cpManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        Set<String> UserTags = event.getWhoClicked().getScoreboardTags();
        ItemStack clickedItem = event.getCurrentItem();

        if(UserTags.contains("MenuOpen")){
            event.setCancelled(true);
        }

        if(clickedItem == null || !clickedItem.hasItemMeta()){
            return;
        }

        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return; //手持ちのアイテムをクリックしたときに反応させない。
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = meta.getDisplayName();
        int currentPage = getCurrentPage(event.getView().getTitle());

        if (clickedItem.getType() == Material.NETHER_STAR) {
            if(!isDeleteMode(player)){
                teleport(player, displayName);
            }
            else {
                cpManager.removeCheckpoint(player.getUniqueId(), player.getWorld(), displayName);
                new CheckpointMenu(player, cpManager, currentPage, isDeleteMode((player))).openInventory();
            }
        }

        else if (clickedItem.getType() == Material.WOOL) {
            short color = clickedItem.getData().getData();
            if (color == 13) { //緑
                deleteModeMap.put(player.getUniqueId(), false);
                new CheckpointMenu(player, cpManager, currentPage, false).openInventory();
            }
            else if(color == 14){ //赤
                deleteModeMap.put(player.getUniqueId(), true);
                new CheckpointMenu(player, cpManager, currentPage, true).openInventory();
            }
        }

        else if(displayName.equals("次へ")){
            new CheckpointMenu(player, cpManager, (currentPage + 1), isDeleteMode(player)).openInventory();
        }

        else if(displayName.equals("前へ")){
            new CheckpointMenu(player, cpManager, (currentPage - 1), isDeleteMode(player)).openInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        //この遅延がないとインベントリを取得できるため。
        Bukkit.getScheduler().runTask(plugin, () -> {
            //インベントリが完全にないとき
            if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                deleteModeMap.remove(player.getUniqueId());
                if (player.getScoreboardTags().contains("MenuOpen")) {
                    player.removeScoreboardTag("MenuOpen");
                }
            }
        });
    }

    private void teleport(Player player, String displayName) {
        if (!cpManager.selectCheckpoint(player.getUniqueId(), player.getWorld(), displayName)) {
            player.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
        }

        else {
            player.teleport(cpManager.getGlobalRecentCpLocation(player.getUniqueId()));
        }
    }

    /**
     *
     * メニュータイトルから現在のページを取得(悪ロジックだけど面倒なのでこれで...)
     *
     * @param title メニュータイトル
     * @return 現在のページ
     */
    private int getCurrentPage(String title){
        String prefix = "(page ";
        int start = title.indexOf(prefix);
        if (start == -1) return 0;

        start += prefix.length();
        int end = title.indexOf(")", start);
        if (end == -1) return 0;

        try {
            return Integer.parseInt(title.substring(start, end)) - 1;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isDeleteMode(Player player) {
        return deleteModeMap.getOrDefault(player.getUniqueId(), false);
    }
}
