package com.github.cresc28.speedrun.core.listener;

import com.github.cresc28.speedrun.core.manager.CheckpointManager;
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        cpManager.loadRecentLocalCp(uuid, player.getWorld());
        cpManager.loadRecentGlobalCp(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location globalRecentCpLoc = cpManager.getGlobalRecentCpLocation(uuid);
        Location localRecentCpLoc = cpManager.getLocalRecentCpLocation(uuid);

        if(localRecentCpLoc != null) cpManager.saveRecentCp(uuid, false, localRecentCpLoc);
        if(globalRecentCpLoc != null) cpManager.saveRecentCp(uuid, true, globalRecentCpLoc);
        cpManager.removeRecentCpFromMap(uuid);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World fromWorld = event.getFrom();
        Location prevWorldRecentCpLoc = cpManager.getLocalRecentCpLocation(uuid);

        if(prevWorldRecentCpLoc != null) cpManager.saveRecentCp(uuid, false, prevWorldRecentCpLoc);
        cpManager.loadRecentLocalCp(uuid, player.getWorld());
    }
}
