package com.akselglyholt.squaremapsimpleclans.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public final class SimpleClansHook {
  private final SimpleClans simpleClans;

  public SimpleClansHook(final JavaPlugin plugin) {
    this.simpleClans = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
    if (this.simpleClans == null) {
      plugin.getLogger().severe("SimpleClans is not installed!");
      return;
    }
  }

  public SimpleClans getSimpleClans() {
    return this.simpleClans;
  }
}
