package fr.jrich.uhcrun.event.player;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;
import fr.jrich.uhcrun.handler.Step;

public class PlayerPortal extends UHCRunListener {

    public PlayerPortal(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (!Step.isStep(Step.IN_GAME) || event.getTo() == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Le nether est désactivé ! Trouvez des biomes forteresses dans le monde normal.");
        }
    }
}
