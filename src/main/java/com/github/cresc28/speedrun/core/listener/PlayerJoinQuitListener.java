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

public class PlayerJoinQuitListener implements Listener {
    private final CheckpointManager cpm;

    public PlayerJoinQuitListener(CheckpointManager cpm){
        this.cpm = cpm;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        cpm.loadRecentLocalCp(uuid, player.getWorld());
        cpm.loadRecentGlobalCp(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location globalRecentCpLoc = cpm.getGlobalRecentCpLocation(uuid);
        Location localRecentCpLoc = cpm.getLocalRecentCpLocation(uuid);

        if(localRecentCpLoc != null) cpm.saveRecentCp(uuid, false, localRecentCpLoc);
        if(globalRecentCpLoc != null) cpm.saveRecentCp(uuid, true, globalRecentCpLoc);
        cpm.removeRecentCpFromMap(uuid);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World fromWorld = event.getFrom();
        Location prevWorldRecentCpLoc = cpm.getLocalRecentCpLocation(uuid);

        if(prevWorldRecentCpLoc != null) cpm.saveRecentCp(uuid, false, prevWorldRecentCpLoc);
        cpm.loadRecentLocalCp(uuid, player.getWorld());
    }
}
