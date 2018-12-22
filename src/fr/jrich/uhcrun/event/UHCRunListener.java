package fr.jrich.uhcrun.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import org.bukkit.event.Listener;

import fr.jrich.uhcrun.UHCRunPlugin;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UHCRunListener implements Listener {
    protected UHCRunPlugin plugin;
}
