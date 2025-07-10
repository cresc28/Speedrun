package com.github.cresc28.speedrun.core.listener;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.data.MenuState;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
import com.github.cresc28.speedrun.gui.WorldMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * インベントリのクリック時、閉じた時の処理。
 */

public class InventoryActionListener implements Listener {
    private final CheckpointManager cpManager;
    private final Map<UUID, MenuState> menuState = new HashMap<>();
    private final JavaPlugin plugin;

    public InventoryActionListener(CheckpointManager cpManager, JavaPlugin plugin) {
        this.cpManager = cpManager;
        this.plugin = plugin;
    }

    /**
     * インベントリをクリックしたときの処理。
     *
     * @param e InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        Set<String> UserTags = e.getWhoClicked().getScoreboardTags();
        ItemStack clickedItem = e.getCurrentItem();

        if(clickedItem == null || !clickedItem.hasItemMeta()){
            return;
        }

        //普通のチェストなどで誤作動させない。
        if(!UserTags.contains("MenuOpen")){ //userTagsのセットはInteractListener内で行っている。
            return;
        }

        if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
            return; //手持ちのアイテムをクリックしたときに反応させない。
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = meta.getDisplayName();
        UUID uuid = player.getUniqueId();
        menuState.putIfAbsent(uuid, new MenuState(player.getWorld()));
        MenuState ms = menuState.get(uuid);

        if (clickedItem.getType() == Material.NETHER_STAR) {
            if(ms.isWorldMode()){ //チェックポイント選択へ切り替えのネザースターを押したときの処理。
                ms.reset();
                new CheckpointMenu(player, cpManager, ms.getCpPage(), player.getWorld(), ms.isDeleteMode()).openInventory();
                return;
            }

            if(!ms.isDeleteMode()){
                teleport(player, ms.getWorld(), displayName);
            }
            else {
                cpManager.removeCheckpoint(player.getUniqueId(), ms.getWorld(), displayName);
                new CheckpointMenu(player, cpManager, ms.getCpPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
        }

        else if (clickedItem.getType() == Material.PAPER) {
            ms.reset();
            World world = Bukkit.getWorld(displayName);
            if(world == null) {
                player.sendMessage("そのワールドは存在しません。");
                return;
            }
            ms.setWorld(world);
            new CheckpointMenu(player, cpManager, ms.getCpPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
        }

        else if (clickedItem.getType() == Material.WOOL) {
            short color = clickedItem.getData().getData();
            if (color == 13) { //緑
                ms.setDeleteMode(false);
                new CheckpointMenu(player, cpManager, ms.getCpPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
            else if(color == 14){ //赤
                ms.setDeleteMode(true);
                new CheckpointMenu(player, cpManager, ms.getCpPage(), ms.getWorld(),ms.isDeleteMode()).openInventory();
            }
        }

        else if(displayName.equals("次へ")){
            if (!ms.isWorldMode()) {
                ms.incrementCpPage();
                new CheckpointMenu(player, cpManager, ms.getCpPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
            else {
                ms.incrementWorldPage();
                new WorldMenu(player, cpManager, ms.getWorldPage()).openInventory();
            }
        }

        else if(displayName.equals("前へ")){
            if (!ms.isWorldMode()) {
                ms.decrementCpPage();
                new CheckpointMenu(player, cpManager, ms.getCpPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
            else {
                ms.decrementWorldPage();
                new WorldMenu(player, cpManager, ms.getWorldPage()).openInventory();
            }
        }

        else if(displayName.equals("ワールド選択へ切り替え")){
            ms.setWorldMode(true);
            new WorldMenu(player, cpManager, 0).openInventory();
        }
    }

    /**
     * インベントリを閉じたときの処理。
     *
     * @param e InventoryCloseEvent
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        //この遅延がないと機能しない。
        Bukkit.getScheduler().runTask(plugin, () -> {
            //インベントリが完全に閉じられたとき
            if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CHEST) {
                UUID uuid = player.getUniqueId();
                menuState.remove(uuid);
                if (player.getScoreboardTags().contains("MenuOpen")) {
                    player.removeScoreboardTag("MenuOpen");
                }
            }
        });
    }

    /**
     * プレイヤーをあるCPにTPさせる。
     *
     * @param player プレイヤー
     * @param world ワールド
     * @param displayName CP名
     */
    private void teleport(Player player, World world, String displayName) {
        if (!cpManager.selectCheckpoint(player.getUniqueId(), world, displayName)) {
            player.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
        }

        else {
            player.teleport(cpManager.getGlobalRecentCpLocation(player.getUniqueId()));
        }
    }
}
