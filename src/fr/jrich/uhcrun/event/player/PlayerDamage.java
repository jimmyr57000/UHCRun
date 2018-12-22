package fr.jrich.uhcrun.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.scheduler.GameRunnable;

public class PlayerDamage extends UHCRunListener {

    public PlayerDamage(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && (!Step.isStep(Step.IN_GAME) || plugin.isSpectator((Player) event.getEntity()) || GameRunnable.godMode)) {
            event.setCancelled(true);
        }
    }
}
