package fr.jrich.uhcrun.event.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class EntityTarget extends UHCRunListener {

    public EntityTarget(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION) || event.getEntity() instanceof Player && plugin.isSpectator((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
