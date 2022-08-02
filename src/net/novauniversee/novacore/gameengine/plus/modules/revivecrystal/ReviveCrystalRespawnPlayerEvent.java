package net.novauniversee.novacore.gameengine.plus.modules.revivecrystal;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReviveCrystalRespawnPlayerEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	private Player player;

	public ReviveCrystalRespawnPlayerEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}