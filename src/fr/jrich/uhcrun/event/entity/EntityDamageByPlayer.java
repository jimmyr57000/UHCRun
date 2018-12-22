package fr.jrich.uhcrun.event.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;

public class EntityDamageByPlayer extends UHCRunListener {

    public EntityDamageByPlayer(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!Step.isStep(Step.IN_GAME) || event.getDamager() instanceof Player && Team.getPlayerTeam((Player) event.getDamager()) == null) {
            event.setCancelled(true);
        }
    }
}
