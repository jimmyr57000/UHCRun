package fr.jrich.uhcrun.event.inventory;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.BrewEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;

public class BrewCraft extends UHCRunListener {

    public BrewCraft(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        if (event.getContents().getIngredient().getType() == Material.GLOWSTONE_DUST) {
            event.setCancelled(true);
        }
    }
}
