package com.akselglyholt.squaremapsimpleclans;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.WorldIdentifier;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ClanMapPlugin extends JavaPlugin {
  private SimpleClans simpleClans;

  // Store tasks to unregister them later (if needed)
  private final Map<WorldIdentifier, ClanMapTask> tasks = new HashMap<>();

  @Override
  public void onEnable() {
    // Ensure the plugin's data folder exists
    File folder = this.getDataFolder();
    if (!folder.exists()) {
      folder.mkdirs(); // Create the data folder if it doesn't exist
    }

    // Ensure the clanhome.png exists in the data folder
    File imageFile = new File(this.getDataFolder(), "clanhome.png");
    if (!imageFile.exists()) {
      // Copy the default image from resources to the data folder
      try (InputStream in = getClass().getResourceAsStream("/clanhome.png")) {
        if (in == null) {
          getLogger().severe("Default image 'clanhome.png' not found in resources!");
          return;
        }

        // Create the file in the data folder
        Files.copy(in, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        getLogger().info("Default 'clanhome.png' image copied to data folder.");
      } catch (IOException e) {
        getLogger().severe("Failed to copy 'clanhome.png' to data folder: " + e.getMessage());
      }
    }

    getLogger().info("ClanMapPlugin enabled!");

    // Check if simpleclans is installed
    simpleClans = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
    if (simpleClans == null) {
      getLogger().severe("SimpleClans is not installed!");
      return;
    }

    // Initialize and add clan markers to Squaremap
    scheduleClanMarkerUpdates();
  }

  @Override
  public void onDisable() {
    getLogger().info("ClanMapPlugin disabled!");
    // Clean up tasks
    this.tasks.values().forEach(ClanMapTask::disable);
    this.tasks.clear();
  }

  @Override
  public void onLoad() {
    getLogger().info("ClanMapPlugin loaded!");
  }

  private void scheduleClanMarkerUpdates() {
    // Fetch the world where the clans are located
    World world = Bukkit.getWorld("world");
    if (world == null) {
      getLogger().severe("World 'world' not found!");
      return;
    }

    // Wait for Squaremap to load
    getServer().getScheduler().runTask(this, () -> {
      while (!isSquaremapLoaded()) {
        try {
          Thread.sleep(100); // Wait for Squaremap to load
        } catch (InterruptedException e) {
          getLogger().warning("Thread sleep interrupted!");
        }
      }

      // Load the image from the resources folder
      try {
        final BufferedImage image = ImageIO.read(new File(this.getDataFolder(), "clanhome.png"));
        SquaremapProvider.get().iconRegistry().register(Key.of("clanhome"), image);
      } catch (Exception e) {
        getLogger().severe("Failed to load image from resources folder: " + e.getMessage());
        return;
      }

      // Create a SimpleLayerProvider for clan markers
      SimpleLayerProvider clanLayer = SimpleLayerProvider.builder("Clan Bases")
          .defaultHidden(false)
          .showControls(true)
          .layerPriority(99)
          .zIndex(1000)
          .build();

      // Register the layer with Squaremap
      SquaremapProvider.get().mapWorlds().forEach(mapWorld -> {
        mapWorld.layerRegistry().register(Key.of("clans"), clanLayer);
      });

      // TODO: make this configurable
      // Task for periodically updating markers every 10 seconds
      getServer().getScheduler().runTaskTimer(this, () -> {
        updateClanMarkers(clanLayer);
      }, 0L, 6000L); // 6000 ticks = 300 seconds - 5 minutes
    });
  }

  private void updateClanMarkers(SimpleLayerProvider clanLayer) {
    // Fetch clans from SimpleClans
    net.sacredlabyrinth.phaed.simpleclans.Clan[] clans = simpleClans.getClanManager().getClans()
        .toArray(new net.sacredlabyrinth.phaed.simpleclans.Clan[0]);

    if (clans.length == 0) {
      // getLogger().info("No clans found in world 'world'!");
      return;
    }

    // Clear all markers from the layer
    clanLayer.clearMarkers();

    // Register each clan marker on the map
    for (net.sacredlabyrinth.phaed.simpleclans.Clan clan : clans) {
      // getLogger().info("Adding clan marker for " + clan.getTag());
      Location homeLocation = clan.getHomeLocation();

      // Skip if clan does not have a home location
      if (homeLocation == null) {
        // getLogger().warning("Clan " + clan.getTag() + " has no home location!");
        continue;
      }

      // Convert the home location to a Point for Squaremap
      Point homePoint = Point.of(homeLocation.getX(), homeLocation.getZ());

      try {
        // Convert InputStream to BufferedImage

        // Create a Key for the image using the path
        Key imageKey = Key.of("clanhome");

        // Create the icon marker using the image and the point
        Icon iconMarker = Marker.icon(homePoint, imageKey, 32, 32);
        if (iconMarker != null) {
          // Set marker properties
          iconMarker.markerOptions(
              MarkerOptions.builder()
                  .hoverTooltip(clan.getName() + " [" + clan.getTag() + "]" + "'s base")
                  .build());

          // Add the marker to the layer
          clanLayer.addMarker(imageKey, iconMarker);
          // getLogger().info("Clan marker for " + clan.getTag() + " added to map.");
        } else {
          getLogger().warning("Failed to create icon for " + clan.getTag());
        }
      } catch (Exception e) {
        getLogger().warning("Failed to load image for clan " + clan.getTag() + ": " + e.getMessage());
      }
    }
  }

  // Helper method to check if Squaremap is loaded
  private boolean isSquaremapLoaded() {
    try {
      return SquaremapProvider.get() != null;
    } catch (IllegalStateException e) {
      return false;
    }
  }

  // Task class for ClanMap (if you need to add periodic updates)
  private static class ClanMapTask {
    public void disable() {
      // Handle task disabling if needed
    }
  }
}
