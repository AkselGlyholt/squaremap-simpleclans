package com.akselglyholt.squaremapsimpleclans.task;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

public final class HideWarringClansTask extends BukkitRunnable {
  private boolean stop;

  public HideWarringClansTask() {
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
        // If the player is not in a war
        // Check if the user is hidden
        if (SquaremapProvider.get().playerManager().hidden(uuid)) {
          // Show the player
          SquaremapProvider.get().playerManager().show(uuid);
        }

        // Continue to next player
        continue;
      }

      // Toggle the player's visibility
      if (!SquaremapProvider.get().playerManager().hidden(uuid)) {
        SquaremapProvider.get().playerManager().hide(uuid);
      }
    }
  }

  public void disable() {
    this.cancel();
    this.stop = true;
  }
}