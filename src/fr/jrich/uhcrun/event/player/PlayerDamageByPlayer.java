package fr.jrich.uhcrun.event.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.scheduler.GameRunnable;

public class PlayerDamageByPlayer extends UHCRunListener {

    public PlayerDamageByPlayer(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (!Step.isStep(Step.IN_GAME)) {
                event.setCancelled(true);
            } else if (GameRunnable.pvpTime > -1) {
                event.setCancelled(true);
                ((Player) event.getDamager()).sendMessage(UHCRunPlugin.prefix + ChatColor.RED + "Le PVP est activé seulement après la téléportation.");
            }
        }
    }
}
