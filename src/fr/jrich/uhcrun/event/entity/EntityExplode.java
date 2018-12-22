package fr.jrich.uhcrun.event.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class EntityExplode extends UHCRunListener {

    public EntityExplode(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION)) {
            event.setCancelled(true);
        }
    }
}
