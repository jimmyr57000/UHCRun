package fr.jrich.uhcrun.event.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

import fr.jrich.uhcrun.UHCRunPlugin;
import fr.jrich.uhcrun.event.UHCRunListener;

public class ChunkUnload extends UHCRunListener {

    public ChunkUnload(UHCRunPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }
}
