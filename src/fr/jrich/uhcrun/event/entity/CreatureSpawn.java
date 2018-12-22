package fr.jrich.uhcrun.event.entity;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class CreatureSpawn extends UHCRunListener {
    public CreatureSpawn(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (Step.isStep(Step.LOBBY)) {
            event.setCancelled(true);
        } else if (event.getEntityType() != EntityType.BLAZE && event.getEntityType() != EntityType.SKELETON) {
            Material under = event.getLocation().subtract(0, 1, 0).getBlock().getType();
            if (under == Material.NETHER_BRICK || under == Material.NETHERRACK || under == Material.QUARTZ_ORE) {
                event.setCancelled(true);
            }
        }
    }
}
