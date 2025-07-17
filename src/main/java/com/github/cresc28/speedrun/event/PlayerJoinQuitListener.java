package com.github.cresc28.speedrun.event;

import com.github.cresc28.speedrun.manager.CheckpointManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * プレイヤーのjoin, quit, ワールド変更時の処理。
 */
public class PlayerJoinQuitListener implements Listener {
    private final CheckpointManager cpManager;

    public PlayerJoinQuitListener(CheckpointManager cpManager){
        this.cpManager = cpManager;
    }

    /**
     * プレイヤー参加時の処理。
     *
     * @param e PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        cpManager.loadRecentLocalCp(uuid, player.getWorld());
        cpManager.loadRecentGlobalCp(uuid);
    }

    /**
     * プレイヤー退出時の処理。
     *
     * @param e PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Location globalRecentCpLoc = cpManager.getGlobalRecentCpLocation(uuid);
        Location localRecentCpLoc = cpManager.getLocalRecentCpLocation(uuid);

        if(localRecentCpLoc != null) cpManager.saveRecentCp(uuid, false, localRecentCpLoc);
        if(globalRecentCpLoc != null) cpManager.saveRecentCp(uuid, true, globalRecentCpLoc);
        cpManager.removeRecentCpFromMap(uuid);
    }

    /**
     * プレイヤーのワールド変更時の処理。
     *
     * @param e PlayerChangedWorldEvent
     */
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        World fromWorld = e.getFrom();
        Location prevWorldRecentCpLoc = cpManager.getLocalRecentCpLocation(uuid);

        if(prevWorldRecentCpLoc != null) cpManager.saveRecentCp(uuid, false, prevWorldRecentCpLoc);
        cpManager.loadRecentLocalCp(uuid, player.getWorld());
    }
}
