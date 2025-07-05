package com.github.cresc28.speedrun.core.listener;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
import com.github.cresc28.speedrun.utils.HeadUtils;
import com.github.cresc28.speedrun.utils.Utils;
import jdk.javadoc.internal.doclets.formats.html.markup.Head;
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

import java.util.Set;

/**
 * インベントリのクリック時、閉じた時の処理。
 */

public class InventoryActionListener implements Listener {
    private final CheckpointManager cpManager;

    public InventoryActionListener(CheckpointManager cpManager) {
        this.cpManager = cpManager;
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

        if (clickedItem.getType() == Material.NETHER_STAR) {
            teleport(player, displayName);
        }

        else if(displayName.equals("次へ")){
            int currentPage = getCurrentPage(event.getView().getTitle());
            new CheckpointMenu(player, cpManager, (currentPage + 1)).openInventory();
        }

        else if(displayName.equals("前へ")){
            int currentPage = getCurrentPage(event.getView().getTitle());
            new CheckpointMenu(player, cpManager, (currentPage - 1)).openInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Set<String> UserTags = event.getPlayer().getScoreboardTags();
        if(UserTags.contains("MenuOpen")){
            event.getPlayer().removeScoreboardTag("MenuOpen");
        }
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
}
