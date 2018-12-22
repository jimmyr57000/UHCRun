package fr.jrich.uhcrun.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.block.Biome;

@AllArgsConstructor
@Getter
public enum GenRule {
    IRON(Material.IRON_ORE, 1, new ArrayList<Biome>(), 0, 64, 8, 3),
    GOLD(Material.GOLD_ORE, 1, new ArrayList<Biome>(), 0, 64, 8, 1),
    LAPIS(Material.LAPIS_ORE, 1, new ArrayList<Biome>(), 0, 64, 4, 2),
    DIAMOND(Material.DIAMOND_ORE, 1, new ArrayList<Biome>(), 0, 64, 4, 2),
    EMERALD(Material.EMERALD_ORE, 0.25F, Arrays.asList(Biome.EXTREME_HILLS, Biome.JUNGLE_HILLS), 0, 64, 1, 1),
    OBSIDIAN(Material.OBSIDIAN, 1, new ArrayList<Biome>(), 0, 24, 3, 8);

    private Material material;
    private float propability;
    private List<Biome> includedBiomes;
    private int minHeight, maxHeight;
    private int size;
    private int rounds;
}
