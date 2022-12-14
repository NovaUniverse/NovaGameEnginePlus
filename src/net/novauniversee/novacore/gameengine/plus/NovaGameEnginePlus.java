package net.novauniversee.novacore.gameengine.plus;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.novauniversee.novacore.gameengine.plus.config.GameEnginePlusItemsAdderConfig;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.explosivearrow.ExplosiveArrowItem;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.shockdart.ShockDartItem;
import net.novauniversee.novacore.gameengine.plus.customitem.revivecrystal.ReviveCrystalItem;
import net.novauniversee.novacore.gameengine.plus.customitem.smokebomb.SmokeGrenade;
import net.novauniversee.novacore.gameengine.plus.modules.revivecrystal.ReviveCrystalConfig;
import net.novauniversee.novacore.gameengine.plus.modules.revivecrystal.ReviveCrystalLoadout;
import net.novauniversee.novacore.gameengine.plus.modules.revivecrystal.ReviveCrystalManager;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModuleManager;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.module.modules.gui.GUIManager;

public class NovaGameEnginePlus extends JavaPlugin {
	private static NovaGameEnginePlus instance;

	public static NovaGameEnginePlus getInstance() {
		return instance;
	}
	
	private GameEnginePlusItemsAdderConfig itemsAdderConfig;

	@Override
	public void onEnable() {
		NovaGameEnginePlus.instance = this;
		saveDefaultConfig();

		this.itemsAdderConfig = new GameEnginePlusItemsAdderConfig(getConfig().getConfigurationSection("ItemsAdder"));
		
		ModuleManager.require(CustomItemManager.class);
		ModuleManager.require(GUIManager.class);
		
		ModuleManager.scanForModules(this, "net.novauniversee.novacore.gameengine.plus.modules");

		MapModuleManager.addMapModule("novagameengineplus.revivecrystal.config", ReviveCrystalConfig.class);
		MapModuleManager.addMapModule("novagameengineplus.revivecrystal.loadout", ReviveCrystalLoadout.class);
		
		try {
			CustomItemManager.getInstance().addCustomItem(ReviveCrystalItem.class);
			CustomItemManager.getInstance().addCustomItem(SmokeGrenade.class);
			
			CustomItemManager.getInstance().addCustomItem(ShockDartItem.class);
			CustomItemManager.getInstance().addCustomItem(ExplosiveArrowItem.class);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			Log.error("NovaGameEnginePlus", "Failed to register custom items");
			e.printStackTrace();
		}

		ReviveCrystalManager.getInstance().setRespawnTimeTicks(getConfig().getInt("ReviveCrystalTime") * 20);

		if (getConfig().getBoolean("RegisterReviveCrystalRecipe")) {
			ReviveCrystalManager.registerRecipe();
		}
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getServer().getScheduler().cancelTasks(this);
	}
	
	public GameEnginePlusItemsAdderConfig getItemsAdderConfig() {
		return itemsAdderConfig;
	}
}