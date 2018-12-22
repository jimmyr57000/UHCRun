package fr.jrich.uhcrun.event.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.BlockData;
import fr.jrich.uhcrun.handler.Step;

public class BlockBreak extends UHCRunListener {
    private Map<Material, List<BlockData>> breakUpgrades = new HashMap<Material, List<BlockData>>() {
        {
            this.put(Material.SAND, Arrays.asList(new BlockData(Material.GLASS)));
            this.put(Material.IRON_ORE, Arrays.asList(new BlockData(Material.IRON_INGOT, 2)));
            this.put(Material.GOLD_ORE, Arrays.asList(new BlockData(Material.GOLD_INGOT, 2)));
            this.put(Material.DIAMOND_ORE, Arrays.asList(new BlockData(Material.DIAMOND, 2)));
            this.put(Material.COAL_ORE, Arrays.asList(new BlockData(Material.TORCH, 2)));
            this.put(Material.GRAVEL, Arrays.asList(new BlockData(Material.FLINT, 1, 0.25F), new BlockData(Material.ARROW, 3)));
        }
    };

    public BlockBreak(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION) || plugin.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        } else {
            Block block = event.getBlock();
            World world = block.getWorld();
            Material type = block.getType();
            if (type == Material.LOG || type == Material.LOG_2) {
                this.breakTree(block, new ArrayList<Block>());
            } else if (breakUpgrades.containsKey(type) && (!type.name().contains("_ORE") || !block.getDrops(event.getPlayer().getItemInHand()).isEmpty())) {
                for (BlockData data : breakUpgrades.get(type)) {
                    if (plugin.getRandom().nextDouble() <= data.getPropability()) {
                        world.dropItemNaturally(block.getLocation(), new ItemStack(data.getMaterial(), data.getAmount()));
                    }
                }
                block.setType(Material.AIR);
            }
        }
    }

    private void breakTree(Block block, List<Block> alreadyChecked) {
        alreadyChecked.add(block);
        for (BlockFace face : new BlockFace[] { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN }) {
            Block relative = block.getRelative(face);
            if (!alreadyChecked.contains(relative) && relative.getType() == block.getType()) {
                this.breakTree(relative, alreadyChecked);
            }
        }
        block.breakNaturally();
    }
}
