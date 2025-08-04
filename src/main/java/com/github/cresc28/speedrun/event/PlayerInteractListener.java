package com.github.cresc28.speedrun.event;


import com.github.cresc28.speedrun.config.ConfigManager;
import com.github.cresc28.speedrun.data.SpeedrunParameters;
import com.github.cresc28.speedrun.db.course.RecordDao;
import com.github.cresc28.speedrun.manager.CheckpointManager;
import com.github.cresc28.speedrun.gui.CheckpointMenu;
import com.github.cresc28.speedrun.utils.GameUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * プレイヤーのアイテムクリック時の処理を行うクラス。
 * ネザースターの右クリックを検知するとチェックポイントへTPさせ、
 * 左クリックを検知するとメニューを開く。
 *
 */

public class PlayerInteractListener implements Listener {
    private final CheckpointManager cpManager;
    private final RecordDao recordDao;

    public PlayerInteractListener(SpeedrunParameters p){
        cpManager = p.getCpManager();
        recordDao = p.getRecordDao();
    }

    /**
     * プレイヤーが何かをクリックしたときの処理。
     *
     * @param e PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = e.getItem();
        Block clickedBlock = e.getClickedBlock();

        //看板クリックよりこちらの処理を優先する。
        if(item != null && item.getType() == Material.NETHER_STAR) {
            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
                handNetherStarRightClick(e);
            }

            else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK){
                handNetherStarLeftClick(e);
            }

            return; //CPでの移動と同時に看板をクリックできないようにする。(不本意なCPセットを防止)
        }

        if (clickedBlock != null && clickedBlock.getState() instanceof Sign && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            signClick(e.getPlayer(), (Sign) clickedBlock.getState());
        }
    }

    /**
     * プレイヤーがネザークリックをもって右クリックしたときの処理。
     *
     * @param e PlayerInteractEvent
     */
    private void handNetherStarRightClick(PlayerInteractEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Location loc;

        if(ConfigManager.isCrossWorldTpAllowed()) loc = cpManager.getGlobalRecentCpLocation(uuid);
        else loc = cpManager.getLocalRecentCpLocation(uuid);

        if (loc != null) {
            player.teleport(loc);
        }
        else {
            player.sendMessage(ChatColor.RED + "有効なチェックポイントが存在しません。");
        }
    }

    /**
     * プレイヤーがネザークリックをもって左クリックしたときの処理。
     *
     * @param e PlayerInteractEvent
     */
    private void handNetherStarLeftClick(PlayerInteractEvent e){
        Player player = e.getPlayer();

        e.setCancelled(true); // ブロック破壊を起こさない
        player.addScoreboardTag("MenuOpen");
        new CheckpointMenu(player, cpManager, 0, player.getWorld(),false).openInventory();
    }

    /**
     * 設置されている看板をクリックしたときの処理
     *
     * @param player クリックしたプレイヤー
     * @param sign 看板
     */
    private void signClick(Player player, Sign sign){
        String firstLine = ChatColor.stripColor(sign.getLine(0));

        if("☆☆☆ Checkpoint ☆☆☆".equals(firstLine)) {
            checkpointResister(player, sign);
        }

        else if("☆☆☆Ranking(All)☆☆☆".equals(firstLine)){
            showRanking(player, sign, true);
        }

        else if("☆☆☆ Ranking ☆☆☆".equals(firstLine)){
            showRanking(player, sign, false);
        }

        else if("☆☆☆Best Record☆☆☆".equals(firstLine)){
            showBestRecord(player, sign);
        }

    }

    /**
     * ベスト記録を表示
     *
     * @param player 表示先のプレイヤー
     * @param sign 押された看板
     */
    private void showBestRecord(Player player, Sign sign) {
        String courseName = ChatColor.stripColor(sign.getLine(1));
        Map.Entry<UUID, Integer> topRecord = recordDao.getTopRecord(courseName);
        if(topRecord == null){
            player.sendMessage("表示できる記録はありません。");
            return;
        }
        UUID topPlayerUuid = topRecord.getKey();
        OfflinePlayer topPlayer = Bukkit.getOfflinePlayer(topPlayerUuid);
        int sumTick = topRecord.getValue();
        player.sendMessage(ChatColor.LIGHT_PURPLE + "----------ベスト記録----------");
        player.sendMessage(ChatColor.GOLD + "1位:" + topPlayer.getName() +",  タイム:" + GameUtils.tickToTime(sumTick));

        Map<String, Integer> viaPointRecord = recordDao.getViaPointRecord(topPlayerUuid, courseName);
        if(viaPointRecord == null || viaPointRecord.isEmpty()) return;

        int prevTick = 0;
        int tick;
        int sectorTime;

        player.sendMessage(ChatColor.LIGHT_PURPLE + "中継地点名:通過タイム  sec:区間タイム");
        for(Map.Entry<String, Integer> entry : viaPointRecord.entrySet()){
            String viaPointName = entry.getKey();
            tick = entry.getValue();

            sectorTime = tick - prevTick;
            prevTick = tick;

            player.sendMessage(ChatColor.GREEN + viaPointName + ":" + GameUtils.tickToTime(tick) + "(" + tick + "ticks)   sec:" + GameUtils.tickToTime(sectorTime) + "(" + sectorTime + "ticks)");
        }

        sectorTime = sumTick - prevTick;
        player.sendMessage(ChatColor.GOLD + "ゴール:" + GameUtils.tickToTime(sumTick) + "(" + sumTick + "ticks)   sec:" + GameUtils.tickToTime(sectorTime) + "(" + sectorTime + "ticks)");
    }

    /**
     * ランキングを表示
     *
     * @param player 表示先のプレイヤー
     * @param sign 押された看板
     * @param allowDup 同一プレイヤーの複数記録を表示するか
     */
    private void showRanking(Player player, Sign sign, Boolean allowDup) {
        String courseName = ChatColor.stripColor(sign.getLine(1));
        String thirdLine = ChatColor.stripColor(sign.getLine(2));
        int displayCount = Integer.parseInt(thirdLine); //thirdLineが数字であるかのチェックはsignChangeListenerにて行っている。
        int rank = 1;
        int displayRank = rank;
        int previousTick = -1;

        List<Map.Entry<String, String>> recordList = allowDup ?
                recordDao.getTopRecordDup(courseName, 1, displayCount) : recordDao.getTopRecordNoDup(courseName, 1, displayCount);


        player.sendMessage(ChatColor.LIGHT_PURPLE + "----------ランキング----------");
        for (Map.Entry<String, String> entry : recordList) {
            int tick = Integer.parseInt(entry.getValue());
            if(tick != previousTick) displayRank = rank;

            String time = GameUtils.tickToTime(tick);
            String line = String.format(ChatColor.GREEN + "%2d. %-16s %10s %6d" + "ticks", displayRank, entry.getKey(), time, tick);
            player.sendMessage(line);

            previousTick = tick;
            rank++;
        }
    }

    /**
     * チェックポイントの登録
     *
     * @param player 登録プレイヤー
     * @param sign 押された看板
     */
    private void checkpointResister(Player player, Sign sign) {
        UUID uuid = player.getUniqueId();
        String courseName = ChatColor.stripColor(sign.getLine(1));

        String type = ChatColor.stripColor(sign.getLine(2));
        if(!"player".equals(type) && !"fixed".equals(type) && !"fly".equals(type)) {
            return;
        }

        Location loc;

        if (type.equals("fixed")) {
            Location blockLoc = sign.getLocation(); //これはブロックの角の座標を返す。
            loc = blockLoc.add(0.5,0,0.5); //ブロックの中心に座標を持ってくる。
            loc.setPitch(0);
            byte signData = sign.getBlock().getData();
            loc.setYaw(GameUtils.getYaw(signData));
        }

        else {
            if ("player".equals(type) && !player.isOnGround()) {
                player.sendMessage("空中でのCP設定は禁止されています");
                return;
            }
            loc = player.getLocation();
        }

        cpManager.registerCheckpoint(uuid, loc, courseName);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1.0f, 1.4f);
    }
}
