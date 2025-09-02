package me.truedarklord.multiBlockHandler.listeners;

import me.truedarklord.multiBlockHandler.MultiBlockHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlockBreak implements Listener {

    private final MultiBlockHandler plugin;

    public BlockBreak(MultiBlockHandler plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockDropItemEvent event) {
        Block currentBlock = event.getBlock();
        Material type = currentBlock.getType();
        List<Item> drops = event.getItems();

        if (!plugin.getConfig().getStringList("whitelist").contains(type.toString())) return;

        while (currentBlock.getType().equals(type)) {

            for (ItemStack drop : currentBlock.getDrops()) {
                drops.add(currentBlock.getWorld().dropItemNaturally(currentBlock.getLocation(), drop));
            }

            currentBlock.setType(Material.AIR);
            currentBlock = currentBlock.getRelative(0, 1, 0);
        }

    }

}
