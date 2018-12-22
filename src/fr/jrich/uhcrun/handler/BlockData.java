package fr.jrich.uhcrun.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.Material;

@AllArgsConstructor
@Getter
public class BlockData {
    private Material material;
    private int amount;
    private float propability;

    public BlockData(Material material) {
        this.material = material;
        amount = 1;
        propability = 1;
    }

    public BlockData(Material material, int amount) {
        this.material = material;
        this.amount = amount;
        propability = 1;
    }
}
