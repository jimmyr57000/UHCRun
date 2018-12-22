package fr.jrich.uhcrun.event.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;

public class EntityRegainHealth extends UHCRunListener {

    public EntityRegainHealth(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getRegainReason() == RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }
}
