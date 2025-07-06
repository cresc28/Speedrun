package com.github.cresc28.speedrun.core.listener;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
import com.github.cresc28.speedrun.data.MenuState;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
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
    List<World> worldList = Bukkit.getWorlds();

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
        UUID uuid = player.getUniqueId();
        menuState.putIfAbsent(uuid, new MenuState(player.getWorld()));
        MenuState ms = menuState.get(uuid);

        if (clickedItem.getType() == Material.NETHER_STAR) {
            if(!ms.isDeleteMode()){
                teleport(player, ms.getWorld(), displayName);
            }
            else {
                cpManager.removeCheckpoint(player.getUniqueId(), ms.getWorld(), displayName);
                new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
        }

        else if (clickedItem.getType() == Material.WOOL) {
            short color = clickedItem.getData().getData();
            if (color == 13) { //緑
                ms.setDeleteMode(false);
                new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
            else if(color == 14){ //赤
                ms.setDeleteMode(true);
                new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(),ms.isDeleteMode()).openInventory();
            }
        }

        else if(displayName.equals("次へ")){
            ms.incrementPage();
            new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
        }

        else if(displayName.equals("前へ")){
            ms.decrementPage();
            new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
        }

        else if(displayName.equals("ワールド切り替え")){
            int index = worldList.indexOf(ms.getWorld());
            int nextIndex = (index + 1) % worldList.size();
            ms.setWorld(worldList.get(nextIndex));
            new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        //この遅延がないとインベントリを取得できるため。
        Bukkit.getScheduler().runTask(plugin, () -> {
            //インベントリが完全にないとき
            if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                UUID uuid = player.getUniqueId();
                menuState.remove(uuid);
                if (player.getScoreboardTags().contains("MenuOpen")) {
                    player.removeScoreboardTag("MenuOpen");
                }
            }
        });
    }

    private void teleport(Player player, World world, String displayName) {
        if (!cpManager.selectCheckpoint(player.getUniqueId(), world, displayName)) {
            player.sendMessage(ChatColor.RED + "指定された名前のチェックポイントは存在しません。");
        }

        else {
            player.teleport(cpManager.getGlobalRecentCpLocation(player.getUniqueId()));
        }
    }
}
