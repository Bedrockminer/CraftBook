package com.sk89q.craftbook;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SelfTriggerThinkEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;

public class SelfTriggeringManager {

    /**
     * List of mechanics that think on a routine basis.
     */
    public final Set<Location> thinkingMechanics = new LinkedHashSet<Location>();

    public void registerSelfTrigger(Chunk chunk) {
        for(BlockState state : chunk.getTileEntities()) {
            Block block = state.getBlock();
            if(thinkingMechanics.contains(block.getLocation())) continue;
            SelfTriggerPingEvent event = new SelfTriggerPingEvent(block);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    public void registerSelfTrigger(Location location) {

        if(thinkingMechanics.contains(location)) return;
        thinkingMechanics.add(location);
    }

    public void unregisterSelfTrigger(Location location, UnregisterReason reason) {

        if(!thinkingMechanics.contains(location)) return;
        SelfTriggerUnregisterEvent event = new SelfTriggerUnregisterEvent(location.getBlock(), reason);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled())
            thinkingMechanics.remove(location);
    }

    public void unregisterSelfTrigger(Chunk chunk) {

        if(thinkingMechanics.size() == 0) return; //Skip the checks this round. Save a little CPU with the array creation.

        Location[] registeredLocations;

        synchronized (this) {
            // Copy to array to get rid of concurrency snafus
            registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
        }

        for (Location location : registeredLocations) {
            if(location.getChunk().equals(chunk))
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
        }
    }

    /**
     * Causes all thinking mechanics to think.
     */
    public void think() {

        if(thinkingMechanics.size() == 0) return; //Skip the checks this round. Save a little CPU with the array creation.

        Location[] registeredLocations;

        synchronized (this) {
            // Copy to array to get rid of concurrency snafus
            registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
        }

        for (Location location : registeredLocations) {
            if(!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
                location.getChunk().load();
            try {
                SelfTriggerThinkEvent event = new SelfTriggerThinkEvent(location.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);
            } catch (Throwable t) { // Mechanic failed to think for some reason
                CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to think for " + location.toString());
                BukkitUtil.printStacktrace(t);
                unregisterSelfTrigger(location, UnregisterReason.ERROR);
            }
        }
    }
}