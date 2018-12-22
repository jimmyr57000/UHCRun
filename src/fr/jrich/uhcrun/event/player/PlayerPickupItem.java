package fr.jrich.uhcrun.event.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;
import fr.jrich.uhcrun.handler.Team;

public class PlayerPickupItem extends UHCRunListener {

    public PlayerPickupItem(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (Step.isStep(Step.LOBBY) || Step.isStep(Step.TELEPORTATION) || Team.getPlayerTeam(event.getPlayer()) == null) {
            event.setCancelled(true);
        }
    }
}
