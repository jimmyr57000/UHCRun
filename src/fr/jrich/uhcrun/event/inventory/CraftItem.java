package fr.jrich.uhcrun.event.inventory;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;

public class CraftItem extends UHCRunListener {

    public CraftItem(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getRecipe() instanceof ShapedRecipe) {
            ShapedRecipe recipe = (ShapedRecipe) event.getRecipe();
            Material type = recipe.getResult().getType();
            if (type == Material.GOLDEN_APPLE && recipe.getResult().getData().getData() == 1) {
                event.setCancelled(true);
                return;
            }
            for (Entry<Character, ItemStack> itemStack : recipe.getIngredientMap().entrySet()) {
                if (type == Material.COMPASS && itemStack.getValue() == null || type == Material.SPECKLED_MELON && itemStack.getValue() != null && itemStack.getValue().getType() == Material.GOLD_NUGGET) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
}
