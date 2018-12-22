package fr.jrich.uhcrun.event.weather;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;

public class WeatherChange extends UHCRunListener {

    public WeatherChange(final UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onWeatherChange(final WeatherChangeEvent event) {
        final World world = event.getWorld();
        if (!world.isThundering() && !world.hasStorm()) {
            event.setCancelled(true);
        }
    }
}
