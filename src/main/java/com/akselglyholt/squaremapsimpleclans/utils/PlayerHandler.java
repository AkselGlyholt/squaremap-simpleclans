package com.akselglyholt.squaremapsimpleclans.utils;

import java.util.UUID;

import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

public final class PlayerHandler {
  public void hidePlayer(UUID uuid) {
    Squaremap squaremap = SquaremapProvider.get();
    squaremap.playerManager().hide(uuid);
  }

  public void showPlayer(UUID uuid) {
    Squaremap squaremap = SquaremapProvider.get();
    squaremap.playerManager().show(uuid);
  }

  public void togglePlayer(final UUID uuid, final boolean show) {
    Squaremap squaremap = SquaremapProvider.get();
    squaremap.playerManager().hide(uuid, show);
  }
}
