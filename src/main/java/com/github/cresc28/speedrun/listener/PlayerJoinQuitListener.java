package com.github.cresc28.speedrun.listener;

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

public class PlayerJoinQuitListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CheckpointManager.loadRecentLocalCp(uuid, player.getWorld());
        CheckpointManager.loadRecentGlobalCp(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location globalRecentCpLoc = CheckpointManager.getGlobalRecentCpLocation(uuid);
        Location localRecentCpLoc = CheckpointManager.getLocalRecentCpLocation(uuid);

        if(localRecentCpLoc != null) CheckpointManager.saveRecentCp(uuid, false, localRecentCpLoc.getWorld(), localRecentCpLoc);
        if(globalRecentCpLoc != null) CheckpointManager.saveRecentCp(uuid, true, globalRecentCpLoc.getWorld(), globalRecentCpLoc);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World fromWorld = event.getFrom();
        Location prevWorldRecentCpLoc = CheckpointManager.getLocalRecentCpLocation(uuid);

        if(prevWorldRecentCpLoc != null) CheckpointManager.saveRecentCp(uuid, false, fromWorld, prevWorldRecentCpLoc);
        CheckpointManager.loadRecentLocalCp(uuid, player.getWorld());
    }
}
