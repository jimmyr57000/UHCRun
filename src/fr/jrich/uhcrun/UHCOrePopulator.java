package fr.jrich.uhcrun;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import fr.jrich.uhcrun.handler.GenRule;

public class UHCOrePopulator extends BlockPopulator {
    private int stackDepth = 0;
    private Queue<DeferredGenerateTask> deferredGenerateTasks;

    private class DeferredGenerateTask {
        private World world;
        private Random random;
        private int cx;
        private int cz;

        public DeferredGenerateTask(World world, Random random, int cx, int cz) {
            this.world = world;
            this.random = random;
            this.cx = cx;
            this.cz = cz;
        }

        public void execute() {
            UHCOrePopulator.this.applyGenerateRules(world, random, world.getChunkAt(cx, cz));
        }
    }

    public UHCOrePopulator() {
        deferredGenerateTasks = new LinkedList<>();
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        // this.applyClearRules(world, chunk);
        this.applyGenerateRules(world, random, chunk);
        if (stackDepth == 0) {
            while (deferredGenerateTasks.size() > 0) {
                DeferredGenerateTask task = deferredGenerateTasks.remove();
                task.execute();
            }
        }
    }

    private void applyGenerateRules(World world, Random random, Chunk chunk) {
        if (stackDepth > 0) {
            deferredGenerateTasks.add(new DeferredGenerateTask(world, random, chunk.getX(), chunk.getZ()));
            return;
        }
        stackDepth += 1;
        for (GenRule rule : GenRule.values()) {
            for (int i = 0; i < rule.getRounds(); i++) {
                if (random.nextDouble() > rule.getPropability()) {
                    continue;
                }
                try {
                    int x = chunk.getX() * 16 + random.nextInt(16);
                    int y = rule.getMinHeight() + random.nextInt(rule.getMaxHeight() - rule.getMinHeight());
                    int z = chunk.getZ() * 16 + random.nextInt(16);
                    if (!rule.getIncludedBiomes().isEmpty()) {
                        Biome biome = world.getBiome(x, z);
                        if (!rule.getIncludedBiomes().contains(biome)) {
                            continue;
                        }
                    }
                    this.generate(world, random, x, y, z, rule.getSize(), rule.getMaterial());
                } catch (NullPointerException ex) {}
            }
        }
        stackDepth -= 1;
    }

    private void applyClearRules(World world, Chunk chunk) {
        stackDepth++;
        for (GenRule rule : GenRule.values()) {
            int starty = rule.getMinHeight();
            int endy = rule.getMaxHeight();
            Material material = rule.getMaterial();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = starty; y < endy; y++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (block.getType() == material) {
                            block.setType(Material.STONE);
                            block.setData((byte) 0);
                        }
                    }
                }
            }
        }
        stackDepth--;
    }

    private void generate(World world, Random rand, int x, int y, int z, int size, Material material) {
        double rpi = rand.nextDouble() * 3.141592653589793D;
        double x1 = x + 8 + Math.sin(rpi) * size / 8.0D;
        double x2 = x + 8 - Math.sin(rpi) * size / 8.0D;
        double z1 = z + 8 + Math.cos(rpi) * size / 8.0D;
        double z2 = z + 8 - Math.cos(rpi) * size / 8.0D;
        double y1 = y + rand.nextInt(3) + 2;
        double y2 = y + rand.nextInt(3) + 2;
        for (int i = 0; i <= size; i++) {
            double xPos = x1 + (x2 - x1) * i / size;
            double yPos = y1 + (y2 - y1) * i / size;
            double zPos = z1 + (z2 - z1) * i / size;
            double fuzz = rand.nextDouble() * size / 16.0D;
            double fuzzXZ = (Math.sin((float) (i * 3.141592653589793D / size)) + 1.0D) * fuzz + 1.0D;
            double fuzzY = (Math.sin((float) (i * 3.141592653589793D / size)) + 1.0D) * fuzz + 1.0D;
            int xStart = (int) Math.floor(xPos - fuzzXZ / 2.0D);
            int yStart = (int) Math.floor(yPos - fuzzY / 2.0D);
            int zStart = (int) Math.floor(zPos - fuzzXZ / 2.0D);
            int xEnd = (int) Math.floor(xPos + fuzzXZ / 2.0D);
            int yEnd = (int) Math.floor(yPos + fuzzY / 2.0D);
            int zEnd = (int) Math.floor(zPos + fuzzXZ / 2.0D);
            for (int ix = xStart; ix <= xEnd; ix++) {
                double xThresh = (ix + 0.5D - xPos) / (fuzzXZ / 2.0D);
                if (xThresh * xThresh < 1.0D) {
                    for (int iy = yStart; iy <= yEnd; iy++) {
                        double yThresh = (iy + 0.5D - yPos) / (fuzzY / 2.0D);
                        if (xThresh * xThresh + yThresh * yThresh < 1.0D) {
                            for (int iz = zStart; iz <= zEnd; iz++) {
                                double zThresh = (iz + 0.5D - zPos) / (fuzzXZ / 2.0D);
                                if (xThresh * xThresh + yThresh * yThresh + zThresh * zThresh < 1.0D) {
                                    Block block = this.tryGetBlock(world, ix, iy, iz);
                                    if (block != null && block.getType() == Material.STONE) {
                                        block.setType(material);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Block tryGetBlock(World world, int x, int y, int z) {
        int cx = x >> 4;
        int cz = z >> 4;
        if (!world.isChunkLoaded(cx, cz) && !world.loadChunk(cx, cz, false)) { return null; }
        Chunk chunk = world.getChunkAt(cx, cz);
        if (chunk == null) { return null; }
        return chunk.getBlock(x & 0xF, y, z & 0xF);
    }
}
