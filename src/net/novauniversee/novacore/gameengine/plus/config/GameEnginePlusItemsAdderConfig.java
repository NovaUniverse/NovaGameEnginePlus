package net.novauniversee.novacore.gameengine.plus.config;

import org.bukkit.configuration.ConfigurationSection;

public class GameEnginePlusItemsAdderConfig {
	private boolean enabled;
	
	private String explosiveArrowItem;
	private String shockArrowItem;
	private String smokeGrenadeItem;
	private String reviveCrystalItem;
	
	public GameEnginePlusItemsAdderConfig(ConfigurationSection config) {
		enabled = config.getBoolean("Enabled");
		
		if(config.getString("ExplosiveArrowItem").length() > 0) {
			explosiveArrowItem = config.getString("ExplosiveArrowItem");
		}
		
		if(config.getString("ShockArrowItem").length() > 0) {
			shockArrowItem = config.getString("ShockArrowItem");
		}
		
		if(config.getString("SmokeGrenadeItem").length() > 0) {
			smokeGrenadeItem = config.getString("SmokeGrenadeItem");
		}
		
		if(config.getString("ReviveCrystalItem").length() > 0) {
			reviveCrystalItem = config.getString("ReviveCrystalItem");
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setenabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getExplosiveArrowItem() {
		return explosiveArrowItem;
	}
	
	public void setExplosiveArrowItem(String explosiveArrowItem) {
		this.explosiveArrowItem = explosiveArrowItem;
	}
	

	public boolean hasExplosiveArrowItem() {
		return explosiveArrowItem != null;
	}
	
	
	public void setShockArrowItem(String shockArrowItem) {
		this.shockArrowItem = shockArrowItem;
	}

	public boolean hasShockArrowItem() {
		return shockArrowItem != null;
	}
	
	public String getShockArrowItem() {
		return shockArrowItem;
	}
	
	public String getSmokeGrenadeItem() {
		return smokeGrenadeItem;
	}
	
	public void setSmokeGrenadeItem(String smokeGrenadeItem) {
		this.smokeGrenadeItem = smokeGrenadeItem;
	}

	public boolean hasSmokeGrenadeItem() {
		return shockArrowItem != null;
	}
	
	
	public String getReviveCrystalItem() {
		return reviveCrystalItem;
	}
	
	public void setReviveCrystalItem(String reviveCrystalItem) {
		this.reviveCrystalItem = reviveCrystalItem;
	}
	

	public boolean hasReviveCrystalItem() {
		return shockArrowItem != null;
	}
}