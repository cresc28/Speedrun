package com.github.cresc28.speedrun.listener;

import com.github.cresc28.speedrun.manager.TimerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final TimerManager timerManager;

    public PlayerMoveListener(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        timerManager.detectStartOrEnd(event.getPlayer());
    }
}