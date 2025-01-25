package com.akselglyholt.squaremapsimpleclans.hook;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.WorldIdentifier;

import com.akselglyholt.squaremapsimpleclans.ClanMap;
import com.akselglyholt.squaremapsimpleclans.task.SquaremapTask;

public final class SquaremapHook {
  public static final Key CLAN_BASE_KEY = Key.of("clanhome");
  public static final Key CLAN_BASE_LAYER_KEY = Key.of("clans");

  private final Map<WorldIdentifier, SquaremapTask> tasks = new HashMap<>();

  public SquaremapHook(final ClanMap plugin, final SimpleClansHook simpleClansHook) {
    // Load clanhome image from resources folder
    try {
      final BufferedImage image = ImageIO.read(new File(plugin.getDataFolder(), "clanhome.png"));
      SquaremapProvider.get().iconRegistry().register(CLAN_BASE_KEY, image);
    } catch (Exception e) {
      plugin.getLogger().severe("Failed to load image from resources folder: " + e.getMessage());
      return;
    }

    // Create a SimpleLayerProvider for clan markers, in each world
    for (final MapWorld world : SquaremapProvider.get().mapWorlds()) {
      SimpleLayerProvider provider = SimpleLayerProvider.builder("Clan Bases")
          .defaultHidden(false)
          .showControls(true)
          .layerPriority(99)
          .zIndex(1000)
          .build();

      // Register the layer with Squaremap
      world.layerRegistry().register(CLAN_BASE_LAYER_KEY, provider);

      // Task for periodically updating markers every 10 seconds
      final SquaremapTask task = new SquaremapTask(plugin, world, provider, simpleClansHook);
      task.runTaskTimerAsynchronously(plugin, 20L, 20L * 300);

      this.tasks.put(world.identifier(), task);
    }

  }

  public void disable() {
    this.tasks.values().forEach(SquaremapTask::disable);
    this.tasks.clear();
  }
}
