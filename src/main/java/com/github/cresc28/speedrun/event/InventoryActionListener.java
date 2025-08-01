package com.github.cresc28.speedrun.event;

import com.github.cresc28.speedrun.data.RecordInfo;
import com.github.cresc28.speedrun.data.RecordSession;
import com.github.cresc28.speedrun.data.SpeedrunParameters;
import com.github.cresc28.speedrun.db.course.RecordDao;
import com.github.cresc28.speedrun.gui.RecordMenuForDelete;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.data.MenuState;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
import com.github.cresc28.speedrun.gui.WorldMenu;
import com.github.cresc28.speedrun.utils.GameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * インベントリのクリック時、閉じた時の処理。
 */
public class InventoryActionListener implements Listener {
    private final CheckpointManager cpManager;
    private final RecordDao recordDao;
    private final RecordSession recordSession;
    private final Map<UUID, MenuState> menuState = new HashMap<>();
    private final JavaPlugin plugin;


    public InventoryActionListener(SpeedrunParameters facade, JavaPlugin plugin) {
        cpManager = facade.getCpManager();
        recordDao = facade.getRecordDao();
        recordSession = facade.getRecordSession();
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
        UUID uuid = player.getUniqueId();
        Set<String> UserTags = e.getWhoClicked().getScoreboardTags();
        ItemStack clickedItem = e.getCurrentItem();

        if(clickedItem == null || !clickedItem.hasItemMeta()) return;
        if(!UserTags.contains("MenuOpen")) return;//普通のチェストなどで誤作動させない。userTagsのセットはInteractListener内で行っている。
        if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) return;  //手持ちのアイテムをクリックしたときに反応させない。

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = meta.getDisplayName();

        menuState.putIfAbsent(uuid, new MenuState(player.getWorld()));
        MenuState ms = menuState.get(uuid);
        String title = e.getView().getTopInventory().getTitle();

        if (clickedItem.getType() == Material.NETHER_STAR) {
            if(title.contains("WorldMenu")){ //チェックポイント選択へ切り替えのネザースターを押したときの処理。
                ms.reset();
                new CheckpointMenu(player, cpManager, ms.getPage(), player.getWorld(), ms.isDeleteMode()).openInventory();
            }

            if(!ms.isDeleteMode()){
                teleport(player, ms.getWorld(), displayName);
            }
            else {
                cpManager.removeCheckpoint(player.getUniqueId(), ms.getWorld(), displayName);
                new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
            return;
        }

        if (clickedItem.getType() == Material.PAPER) {
            if(title.contains("WorldMenu")){
                ms.reset();
                World world = Bukkit.getWorld(displayName);
                if(world == null) {
                    player.sendMessage("そのワールドは存在しません。");
                    return;
                }
                ms.setWorld(world);
                new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
                return;
            }

            else if(title.contains("削除する記録をクリック")){
                String timeString = clickedItem.getItemMeta().getDisplayName();
                RecordInfo info = recordSession.getRecord(uuid);
                String courseName = info.getCourseName();
                UUID targetUuid = info.getTargetUuid();
                int tick = GameUtils.timeStringToTick(timeString);

                ms.reset();
                if(recordDao.delete(targetUuid, courseName, tick)) player.sendMessage("削除に成功しました。");
                List<Integer> times = recordDao.getFinishTick(targetUuid, courseName);
                List<String> updatedTimesString = times.stream().map(GameUtils::tickToTime).collect(Collectors.toList());
                recordSession.setRecord(uuid, new RecordInfo(courseName, targetUuid, updatedTimesString));
                new RecordMenuForDelete(player, updatedTimesString, ms.getPage()).openInventory();
            }
        }

        if (clickedItem.getType() == Material.WOOL) {
            short color = clickedItem.getData().getData();
            if (color == 13) { //緑
                ms.setDeleteMode(false);
                new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            }
            else if(color == 14){ //赤
                ms.setDeleteMode(true);
                new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(),ms.isDeleteMode()).openInventory();
            }
            return;
        }

        if(displayName.equals("次へ")){
            ms.incrementPage();
            if (title.contains("CheckpointMenu")) new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            else if(title.contains("WorldMenu")) new WorldMenu(player, cpManager, ms.getPage()).openInventory();
            else if(title.contains("削除する記録")) new RecordMenuForDelete(player, recordSession.getRecord(uuid).getRecordTimes(), ms.getPage()).openInventory();
            return;
        }

        if(displayName.equals("前へ")){
            ms.decrementPage();
            if (title.contains("CheckpointMenu")) new CheckpointMenu(player, cpManager, ms.getPage(), ms.getWorld(), ms.isDeleteMode()).openInventory();
            else if(title.contains("WorldMenu")) new WorldMenu(player, cpManager, ms.getPage()).openInventory();
            else if(title.contains("削除する記録")) new RecordMenuForDelete(player, recordSession.getRecord(uuid).getRecordTimes(), ms.getPage()).openInventory();
            return;
        }

        if(displayName.equals("ワールド選択へ切り替え")){
            ms.reset();
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
