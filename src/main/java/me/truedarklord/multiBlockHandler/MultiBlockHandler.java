package me.truedarklord.multiBlockHandler;

import me.truedarklord.multiBlockHandler.listeners.BlockBreak;
import org.bukkit.plugin.java.JavaPlugin;

public final class MultiBlockHandler extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new BlockBreak(this);
    }

}
