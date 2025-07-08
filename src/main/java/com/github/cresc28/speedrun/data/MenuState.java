package com.github.cresc28.speedrun.data;

import org.bukkit.World;

/**
 *
 * メニューの状態管理クラス。
 */
public class MenuState {
    boolean deleteMode = false;
    boolean worldMode = false;
    int cpPage = 0;
    int worldPage = 0;
    World selectedWorld;

    public MenuState(World initialWorld) {
        this.selectedWorld = initialWorld;
    }
    public boolean isDeleteMode() {
        return deleteMode;
    }

    public void setDeleteMode(boolean isDeleteMode) {
        this.deleteMode = isDeleteMode;
    }

    public boolean isWorldMode() {
        return worldMode;
    }

    public void setWorldMode(boolean isWorldMode) {
        this.worldMode = isWorldMode;
    }

    public int getCpPage() {
        return cpPage;
    }

    public void incrementCpPage() {
        this.cpPage++;
    }

    public void decrementCpPage() {
        this.cpPage--;
    }

    public int getWorldPage() {
        return worldPage;
    }

    public void incrementWorldPage() {
        this.worldPage++;
    }

    public void decrementWorldPage() {
        this.worldPage--;
    }

    public World getWorld() {
        return selectedWorld;
    }

    public void setWorld(World world) {
        this.selectedWorld = world;
    }

    public void reset(){
        this.cpPage = 0;
        this.worldPage = 0;
        this.deleteMode = false;
        this.worldMode = false;
    }
}
