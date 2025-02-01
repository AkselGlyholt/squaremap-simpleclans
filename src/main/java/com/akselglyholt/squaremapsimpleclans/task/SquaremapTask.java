package com.akselglyholt.squaremapsimpleclans.task;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import com.akselglyholt.squaremapsimpleclans.ClanMap;
import com.akselglyholt.squaremapsimpleclans.hook.SimpleClansHook;

import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

public final class SquaremapTask extends BukkitRunnable {
  private final World bukkitWorld;
  private final SimpleLayerProvider provider;
  private final ClanMap plugin;
  private final SimpleClansHook simpleClansHook;

  private boolean stop;

  public SquaremapTask(
      final ClanMap plugin,
      final MapWorld world,
      final SimpleLayerProvider provider,
      final SimpleClansHook simpleClansHook) {
    this.plugin = plugin;
    this.bukkitWorld = BukkitAdapter.bukkitWorld(world);
    this.provider = provider;
    this.simpleClansHook = simpleClansHook;
  }

  @Override
  public void run() {
    if (this.stop) {
      this.cancel();
    }
    this.updateBases();
  }

  void updateBases() {
    this.provider.clearMarkers();

    // Make sure simpleClansHook is not null
    SimpleClans simpleClans = simpleClansHook.getSimpleClans();

    // Fetch clans from SimpleClans
    Clan[] clans = simpleClans.getClanManager().getClans()
        .toArray(new Clan[0]);

    if (clans.length == 0) {
      // getLogger().info("No clans found in world 'world'!");
      return;
    }

    // Register each clan makere on the map
    for (Clan clan : clans) {
      Location homeLocation = clan.getHomeLocation();
      // Create custom icon for clan base
      final Key CUSTOM_CLAN_BASE_KEY = Key.of("clanhome-" + clan.getTag());

      // Check if clan home world is the same as the world we are in
      if (homeLocation == null || homeLocation.getWorld() != this.bukkitWorld) {
        // Remove the marker if it exists
        this.provider.removeMarker(CUSTOM_CLAN_BASE_KEY);
        continue;
      }

      // Conver the home location to a Point for Squaremap
      Point homePoint = Point.of(homeLocation.getX(), homeLocation.getZ());

      // Create an Icon Point for the clan base
      try {
        // Load clanhome image from resources folder
        if (!SquaremapProvider.get().iconRegistry().hasEntry(CUSTOM_CLAN_BASE_KEY)) {
          try {
            final BufferedImage image = ImageIO.read(new File(plugin.getDataFolder(), "clanhome.png"));
            SquaremapProvider.get().iconRegistry().register(CUSTOM_CLAN_BASE_KEY, image);
            plugin.getLogger().info("Clan base icon successfully registered." + clan.getTag());
          } catch (Exception e) {
            plugin.getLogger().severe("Failed to load image from resources folder: " + e.getMessage());
          }
        } else {
          plugin.getLogger().info("Clan base icon already registered.");
        }

        // Generate a unique key for each clan base
        Key imageKey = Key.of("clanhome-" + clan.getTag());

        // Create the icon marker using the image and the point
        Icon iconMarker = Marker.icon(homePoint, imageKey, 32, 32);
        if (iconMarker != null) {
          // Set marker properties
          iconMarker.markerOptions(
              MarkerOptions.builder()
                  .hoverTooltip(clan.getName() + " [" + clan.getTag() + "]" + "'s base")
                  .build());

          // Add the marker to the layer
          this.provider.addMarker(imageKey, iconMarker);
          // getLogger().info("Clan marker for " + clan.getTag() + " added to map.");
        } else {
          plugin.getLogger().warning("Failed to create icon for " + clan.getTag());
        }
      } catch (Exception e) {
        plugin.getLogger().warning("Failed to load image for clan " + clan.getTag() + ": " + e.getMessage());
        continue;
      }
    }
  }

  public void disable() {
    this.cancel();
    this.stop = true;
    this.provider.clearMarkers();
  }
}
