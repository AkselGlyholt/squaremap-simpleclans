package com.akselglyholt.squaremapsimpleclans;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.akselglyholt.squaremapsimpleclans.hook.SimpleClansHook;
import com.akselglyholt.squaremapsimpleclans.hook.SquaremapHook;

public final class ClanMap extends JavaPlugin {
  private SquaremapHook squaremapHook;
  private SimpleClansHook simpleClansHook;

  @Override
  public void onEnable() {
    // Ensure the plugin's data folder exists
    File folder = this.getDataFolder();
    if (!folder.exists()) {
      folder.mkdirs(); // Create the data folder if it doesn't exist
    }

    saveDefaultResources();

    this.simpleClansHook = new SimpleClansHook(this);
    this.squaremapHook = new SquaremapHook(this, simpleClansHook);
  }

  @Override
  public void onDisable() {
    this.squaremapHook.disable();
  }

  private void saveDefaultResources() {
    // List all resources in the resources folder
    String[] resources = {
        "config.yml",
        "clanhome.png"
    };

    // Loop through each resource
    for (String resource : resources) {
      File destination = new File(getDataFolder(), resource);
      if (!destination.exists()) {
        saveResource(resource, isEnabled());
      }
    }
  }
}