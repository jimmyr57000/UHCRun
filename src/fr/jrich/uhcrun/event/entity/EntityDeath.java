package fr.jrich.uhcrun.event.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class EntityDeath extends UHCRunListener {
    private Map<Material, Material> dropUpgrades = new HashMap<Material, Material>() {
        {
            this.put(Material.RAW_BEEF, Material.COOKED_BEEF);
            this.put(Material.RAW_CHICKEN, Material.COOKED_CHICKEN);
            this.put(Material.PORK, Material.GRILLED_PORK);
            this.put(Material.GHAST_TEAR, Material.GOLD_INGOT);
        }
    };

    public EntityDeath(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION)) {
            event.setDroppedExp(0);
        } else {
            event.setDroppedExp(event.getDroppedExp() * 2);
            for (ItemStack drop : new ArrayList<>(event.getDrops())) {
                if (dropUpgrades.containsKey(drop.getType())) {
                    drop.setType(dropUpgrades.get(drop.getType()));
                }
                if (event.getEntity() instanceof Monster && drop.getType().getMaxStackSize() >= drop.getAmount() * 2) {
                    drop.setAmount(drop.getAmount() * 2);
                } else if (event.getEntity() instanceof Animals && plugin.getRandom().nextInt(10) == 1) {
                    event.getDrops().add(new ItemStack(Material.LEATHER, plugin.getRandom().nextInt(3) + 1));
                }
            }
            EntityType type = event.getEntity().getType();
            if (type == EntityType.SHEEP) {
                event.getDrops().add(new ItemStack(Material.COOKED_CHICKEN, plugin.getRandom().nextInt(3) + 1));
            } else if (type == EntityType.CHICKEN) {
                int arrows = plugin.getRandom().nextInt(4);
                if (arrows > 0) {
                    event.getDrops().add(new ItemStack(Material.ARROW, arrows));
                }
            }
        }
    }
}
