package me.truedarklord.multiBlockHandler.listeners;

import me.truedarklord.multiBlockHandler.MultiBlockHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlockBreak implements Listener {

    private final MultiBlockHandler plugin;

    public BlockBreak(MultiBlockHandler plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockDropItemEvent event) {
        Material type = event.getBlockState().getType();
        FileConfiguration config = plugin.getConfig();
        Block currentBlock = event.getBlock().getRelative(0, 1, 0);
        List<Block> blocks = new ArrayList<>();
        List<Item> drops = event.getItems();
        int offset = 0;

        if (config.getStringList("above-whitelist").contains(type.toString())) offset = 1;
        if (config.getStringList("below-whitelist").contains(type.toString())) offset = -1;

        if (offset == 0) return;

        List<String> byproduct = config.getStringList("byproduct." + type);

        while (currentBlock.getType().equals(type) || byproduct.contains(currentBlock.getType().toString())) {

            for (ItemStack drop : currentBlock.getDrops()) {
                drops.add(currentBlock.getWorld().dropItemNaturally(currentBlock.getLocation(), drop));
            }

            blocks.add(currentBlock);
            currentBlock = currentBlock.getRelative(0, offset, 0);
        }

        /*
            Due to how multi-block structures work:
              All drops need to be captured prior to setting blocks to air.
              Setting blocks to air needs to be reversed.
         */
        for (int i = blocks.size() - 1; i >= 0; i--) {
            blocks.get(i).setType(Material.AIR);
        }

    }

}
