package com.akselglyholt.squaremapsimpleclans.hook;

import org.bukkit.Bukkit;

import com.akselglyholt.squaremapsimpleclans.ClanMap;
import com.akselglyholt.squaremapsimpleclans.task.HideWarringClansTask;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public final class SimpleClansHook {
  private final SimpleClans simpleClans;
  private final HideWarringClansTask hideWarringClansTask;

  public SimpleClansHook(final ClanMap plugin) {
    this.simpleClans = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
    if (this.simpleClans == null) {
      plugin.getLogger().severe("SimpleClans is not installed!");
      this.hideWarringClansTask = null; // Make sure hideWarringClansTask is initialized
      return;
    }

    // Start the HideWarringClansTask to update every 60 seconds (1 minute)
    this.hideWarringClansTask = new HideWarringClansTask(plugin);
    this.hideWarringClansTask.runTaskTimerAsynchronously(plugin, 0L, 20L * 60); // Checks for wars every minute
  }

  public SimpleClans getSimpleClans() {
    return this.simpleClans;
  }

  public void disable() {
    if (this.hideWarringClansTask != null) {
      this.hideWarringClansTask.disable();
    }
  }
}
