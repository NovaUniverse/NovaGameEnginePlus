package net.novauniversee.novacore.gameengine.plus.modules.revivecrystal;

import org.json.JSONObject;

import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule;

public class ReviveCrystalConfig extends MapModule {
	private int maxY;
	private int respawnTime;

	public ReviveCrystalConfig(JSONObject json) {
		super(json);

		maxY = -1;
		respawnTime = -1;

		if (json.has("max_y")) {
			maxY = json.getInt("max_y");
		}

		if (json.has("respawn_time")) {
			respawnTime = json.getInt("respawn_time");
		}
	}

	public int getMaxY() {
		return maxY;
	}

	public int getRespawnTime() {
		return respawnTime;
	}
}