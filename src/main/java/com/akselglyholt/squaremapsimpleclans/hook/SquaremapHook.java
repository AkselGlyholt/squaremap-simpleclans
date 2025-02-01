package com.akselglyholt.squaremapsimpleclans.hook;

import java.util.HashMap;
import java.util.Map;

import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.WorldIdentifier;

import com.akselglyholt.squaremapsimpleclans.ClanMap;
import com.akselglyholt.squaremapsimpleclans.task.SquaremapTask;

public final class SquaremapHook {
  public static final Key CLAN_BASE_LAYER_KEY = Key.of("clans");

  private final Map<WorldIdentifier, SquaremapTask> tasks = new HashMap<>();

  public SquaremapHook(final ClanMap plugin, final SimpleClansHook simpleClansHook) {
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
      task.runTaskTimerAsynchronously(plugin, 20L, 20L * 5 /* 300 */);

      this.tasks.put(world.identifier(), task);
    }

  }

  public void disable() {
    this.tasks.values().forEach(SquaremapTask::disable);
    this.tasks.clear();
  }
}
