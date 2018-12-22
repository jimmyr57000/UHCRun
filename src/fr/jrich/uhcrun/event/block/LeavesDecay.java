package fr.jrich.uhcrun.event.block;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class LeavesDecay extends UHCRunListener {

    public LeavesDecay(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION)) {
            event.setCancelled(true);
        } else {
            Block block = event.getBlock();
            World world = block.getWorld();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            this.breakRadiusIfLeaf(world.getBlockAt(x - 1, y - 1, z - 1));
            this.breakRadiusIfLeaf(world.getBlockAt(x - 1, y - 1, z + 1));
            this.breakRadiusIfLeaf(world.getBlockAt(x - 1, y + 1, z - 1));
            this.breakRadiusIfLeaf(world.getBlockAt(x - 1, y + 1, z + 1));
            this.breakRadiusIfLeaf(world.getBlockAt(x + 1, y - 1, z - 1));
            this.breakRadiusIfLeaf(world.getBlockAt(x + 1, y - 1, z + 1));
            this.breakRadiusIfLeaf(world.getBlockAt(x + 1, y + 1, z - 1));
            this.breakRadiusIfLeaf(world.getBlockAt(x + 1, y + 1, z + 1));
        }
    }

    private void breakIfLonelyLeaf(Block blockAt) {
        if (blockAt.getType() != Material.LEAVES && blockAt.getType() != Material.LEAVES_2) { return; }
        World world = blockAt.getWorld();
        int fail = -1;
        for (int x = blockAt.getX() - 2; x <= blockAt.getX() + 2; x++) {
            for (int y = blockAt.getY() - 2; y <= blockAt.getY() + 2; y++) {
                for (int z = blockAt.getZ() - 2; z <= blockAt.getZ() + 2; z++) {
                    fail += this.calcAir(world.getBlockAt(x, y, z));
                    if (fail > 4) { return; }
                }
            }
        }
        blockAt.breakNaturally();
    }

    private void breakRadiusIfLeaf(Block blockAt) {
        if (blockAt.getType() == Material.LEAVES || blockAt.getType() == Material.LEAVES_2) {
            blockAt.breakNaturally();
            World world = blockAt.getWorld();
            int x = blockAt.getX();
            int y = blockAt.getY();
            int z = blockAt.getZ();
            for (int x2 = -8; x2 < 9; x2++) {
                for (int z2 = -8; z2 < 9; z2++) {
                    this.breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 2, z + z2));
                    this.breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 1, z + z2));
                    this.breakIfLonelyLeaf(world.getBlockAt(x + x2, y, z + z2));
                    this.breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 1, z + z2));
                    this.breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 2, z + z2));
                }
            }
        }
    }

    private int calcAir(Block blockAt) {
        if (blockAt.getType() == Material.AIR || blockAt.getType() == Material.VINE) {
            return 0;
        } else if (blockAt.getType() == Material.LOG || blockAt.getType() == Material.LOG_2) {
            return 5;
        } else {
            return 1;
        }
    }
}
