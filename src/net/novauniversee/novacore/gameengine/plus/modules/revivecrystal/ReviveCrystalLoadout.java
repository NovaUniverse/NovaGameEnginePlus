package net.novauniversee.novacore.gameengine.plus.modules.revivecrystal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule;
import net.zeeraa.novacore.spigot.utils.JSONItemParser;

public class ReviveCrystalLoadout extends MapModule {
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;

	private List<ItemStack> items;

	public ReviveCrystalLoadout(JSONObject json) {
		super(json);

		helmet = null;
		chestplate = null;
		leggings = null;
		boots = null;
		items = new ArrayList<>();

		if (json.has("helmet")) {
			try {
				helmet = JSONItemParser.itemFromJSON(json.getJSONObject("helmet"));
			} catch (JSONException | IOException e) {
				Log.error("ReviveCrystalLoadout", "Failed to parse helmet item. " + e.getClass() + " " + e.getMessage());
			}
		}

		if (json.has("chestplate")) {
			try {
				chestplate = JSONItemParser.itemFromJSON(json.getJSONObject("chestplate"));
			} catch (JSONException | IOException e) {
				Log.error("ReviveCrystalLoadout", "Failed to parse chestplate item. " + e.getClass() + " " + e.getMessage());
			}
		}

		if (json.has("leggings")) {
			try {
				leggings = JSONItemParser.itemFromJSON(json.getJSONObject("leggings"));
			} catch (JSONException | IOException e) {
				Log.error("ReviveCrystalLoadout", "Failed to parse leggings item. " + e.getClass() + " " + e.getMessage());
			}
		}

		if (json.has("boots")) {
			try {
				boots = JSONItemParser.itemFromJSON(json.getJSONObject("boots"));
			} catch (JSONException | IOException e) {
				Log.error("ReviveCrystalLoadout", "Failed to parse boots item. " + e.getClass() + " " + e.getMessage());
			}
		}

		if (json.has("items")) {
			JSONArray itemsJson = new JSONArray();
			for (int i = 0; i < itemsJson.length(); i++) {
				JSONObject item = itemsJson.getJSONObject(i);
				try {
					items.add(JSONItemParser.itemFromJSON(item));
				} catch (IOException e) {
					Log.error("ReviveCrystalLoadout", "Failed to parse item. " + e.getClass() + " " + e.getMessage());
				}
			}
		}
	}

	public boolean hasHelmet() {
		return helmet != null;
	}

	public boolean hasChestplate() {
		return chestplate != null;
	}

	public boolean hasLeggings() {
		return leggings != null;
	}

	public boolean hasBoots() {
		return boots != null;
	}

	public ItemStack getHelmet() {
		return helmet;
	}

	public ItemStack getChestplate() {
		return chestplate;
	}

	public ItemStack getLeggings() {
		return leggings;
	}

	public ItemStack getBoots() {
		return boots;
	}

	public List<ItemStack> getItems() {
		return items;
	}

	public void apply(Player player) {
		if (hasHelmet()) {
			player.getInventory().setHelmet(getHelmet().clone());
		}

		if (hasChestplate()) {
			player.getInventory().setChestplate(getChestplate().clone());
		}

		if (hasLeggings()) {
			player.getInventory().setLeggings(getLeggings().clone());
		}

		if (hasBoots()) {
			player.getInventory().setBoots(getBoots().clone());
		}

		items.forEach(item -> player.getInventory().addItem(item.clone()));
	}
}