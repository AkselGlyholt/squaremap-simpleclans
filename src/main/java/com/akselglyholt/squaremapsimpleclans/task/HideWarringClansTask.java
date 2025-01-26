package com.akselglyholt.squaremapsimpleclans.task;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.akselglyholt.squaremapsimpleclans.ClanMap;

import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

public final class HideWarringClansTask extends BukkitRunnable {
  private final ClanMap plugin;

  private boolean stop;

  public HideWarringClansTask(
      final ClanMap plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    if (this.stop) {
      this.cancel();
    }
    this.updatePlayers();
  }

  // Toggle players if in war
  void updatePlayers() {
    // Loop through all online players
    for (Player player : Bukkit.getOnlinePlayers()) {
      final UUID uuid = player.getUniqueId();
      // Get the clan player from SimpleClans
      final ClanPlayer clanPlayer = SimpleClans.getInstance().getClanManager().getClanPlayer(player);
      if (clanPlayer == null) {
        continue;
      }

      // Check if the player is in a war
      if (clanPlayer.getClan().getWarringClans().isEmpty()) {
        // If the player is not in a war, continue
        continue;
      }

      // Check if the player is hidden
      boolean hide = SquaremapProvider.get().playerManager().hidden(uuid);
      plugin.getLogger().info("Hiding " + player.getName() + " from map: " + hide);

      // Toggle the player's visibility
      if (hide && !SquaremapProvider.get().playerManager().hidden(uuid)) {
        // Hide the player
        SquaremapProvider.get().playerManager().hide(uuid);
      } else if (!hide && SquaremapProvider.get().playerManager().hidden(uuid)) {
        // Show the player
        SquaremapProvider.get().playerManager().show(uuid);
      }
    }
  }

  public void disable() {
    this.cancel();
    this.stop = true;
  }
}