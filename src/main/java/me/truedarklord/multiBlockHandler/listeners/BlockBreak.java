package me.truedarklord.multiBlockHandler.listeners;

import me.truedarklord.multiBlockHandler.MultiBlockHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
    public void onDrop(BlockDropItemEvent event) {
        if (event.getBlock().getBlockData() instanceof CaveVinesPlant) return;

        Material type = event.getBlockState().getType();
        FileConfiguration config = plugin.getConfig();
        Block currentBlock;
        List<Block> blocks = new ArrayList<>();
        List<Item> drops = event.getItems();
        int offset = 0;

        if (config.getStringList("above-whitelist").contains(type.toString())) offset = 1;
        if (config.getStringList("below-whitelist").contains(type.toString())) offset = -1;
        currentBlock = event.getBlock().getRelative(0, offset, 0);

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
            Block block = blocks.get(i);

            if (block.getType().equals(Material.KELP) || block.getType().equals(Material.KELP_PLANT)) {
                block.setType(Material.WATER);
                continue;
            }

            blocks.get(i).setType(Material.AIR);
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (!(event.isDropItems())) return;
        if (!(event.getBlock().getBlockData() instanceof CaveVinesPlant)) return;

        Block firstBlock = event.getBlock().getRelative(0, -1, 0);
        Block currentBlock = firstBlock;
        List<Item> drops = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();

        while (popBerries(currentBlock, drops)) {
            blocks.add(currentBlock);
            currentBlock = currentBlock.getRelative(0, -1, 0);
        }

        BlockDropItemEvent dropEvent = new BlockDropItemEvent(firstBlock, firstBlock.getState(), event.getPlayer(), drops);
        Bukkit.getPluginManager().callEvent(dropEvent);

        if (dropEvent.isCancelled()) drops.forEach(Entity::remove);

        blocks.forEach(block -> block.setType(Material.AIR));

    }

    /**
     * Gathers and stores berries in the drops of an item.
     * @param block The block to check for berries.
     * @param drops The list of drops to add the item to.
     * @return True if the block is a CaveVine.
     */
    private boolean popBerries(Block block, List<Item> drops) {
        if (!(block.getBlockData() instanceof CaveVinesPlant plant)) return false;

        if (!plant.isBerries()) return true;

        plant.setBerries(false);
        drops.add(block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GLOW_BERRIES)));

        block.setBlockData(plant);

        return true;
    }

}
