package net.novauniversee.novacore.gameengine.plus.modules.hologramhitmarkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.md_5.bungee.api.ChatColor;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.commons.utils.NumberUtils;
import net.zeeraa.novacore.commons.utils.RandomGenerator;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.annotations.NovaAutoLoad;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.utils.NovaItemsAdderUtils;

@NovaAutoLoad(shouldEnable = true)
public class HologramHitmarkers extends NovaModule {
	private static HologramHitmarkers instance;

	private double randomOffset = 1D;
	private double yOffset = 0.5D;
	private double movementSpeed = 0.02D;

	private int hologramLifetime = 20 * 3;

	private String itemsAdderHeartIcon = null;

	private Random random = new Random();

	private Task task;

	private List<HitmarkerHologramWrapper> wrappers;

	public static HologramHitmarkers getInstance() {
		return instance;
	}

	public HologramHitmarkers() {
		super("NovaGameenginePlus.HologramHitmarkers");

		HologramHitmarkers.instance = this;
	}

	@Override
	public void onLoad() {
		wrappers = new ArrayList<>();
		task = new SimpleTask(getPlugin(), () -> {
			wrappers.forEach(HitmarkerHologramWrapper::tick);
			wrappers.removeIf(HitmarkerHologramWrapper::isRemoved);
		}, 0L, 0L);
	}

	@Override
	public void onEnable() throws Exception {
		Task.tryStartTask(task);
	}

	@Override
	public void onDisable() throws Exception {
		Task.tryStopTask(task);
		wrappers.forEach(HitmarkerHologramWrapper::destroy);
		wrappers.clear();
	}

	public String getItemsAdderHeartIcon() {
		return itemsAdderHeartIcon;
	}

	public void setItemsAdderHeartIcon(String itemsAdderHeartIcon) {
		this.itemsAdderHeartIcon = itemsAdderHeartIcon;
	}

	public double getRandomOffset() {
		return randomOffset;
	}

	public double getYOffset() {
		return yOffset;
	}

	public int getHologramLifetime() {
		return hologramLifetime;
	}

	public double getMovementSpeed() {
		return movementSpeed;
	}

	public void setRandomOffset(double randomOffset) {
		this.randomOffset = randomOffset;
	}

	public void setYOffset(double yOffset) {
		this.yOffset = yOffset;
	}

	public void setHologramLifetime(int hologramLifetime) {
		this.hologramLifetime = hologramLifetime;
	}

	public void setMovementSpeed(double movementSpeed) {
		this.movementSpeed = movementSpeed;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public void showHitmarker(Player damager, Player player, double damage) {
		this.showHitmarker(damager, player, damage, 2, false);
	}

	public void showHitmarker(Player damager, Player player, double damage, int precision) {
		this.showHitmarker(damager, player, damage, precision, false);
	}

	public void showHitmarker(Player damager, Player player, double damage, boolean showOnlyForDamager) {
		this.showHitmarker(damager, player, damage, 2, showOnlyForDamager);
	}

	public void showHitmarker(Player damager, Player player, double damage, int precision, boolean showOnlyForDamager) {
		Location location = player.getEyeLocation().clone();

		location.add(RandomGenerator.generateDouble(randomOffset * -1, randomOffset, random), yOffset, RandomGenerator.generateDouble(randomOffset * -1, randomOffset, random));

		Hologram hologram = HologramsAPI.createHologram(getPlugin(), location);

		ChatColor color = ChatColor.GRAY;
		String symbol = "";

		if (damage > 0) {
			color = ChatColor.RED;
			symbol = "-";
		} else if (damage < 0) {
			damage *= -1;
			color = ChatColor.GREEN;
			symbol = "+";
		}

		String suffix = color + " hp";
		if (itemsAdderHeartIcon != null) {
			suffix = " " + NovaItemsAdderUtils.getFontImage(itemsAdderHeartIcon);
		}

		String text = color + symbol + NumberUtils.round(damage, precision) + suffix;

		hologram.appendTextLine(text);

		if (showOnlyForDamager) {
			hologram.getVisibilityManager().setVisibleByDefault(false);
			hologram.getVisibilityManager().showTo(damager);
		}

		wrappers.add(new HitmarkerHologramWrapper(hologram, hologramLifetime, movementSpeed));
	}
}

class HitmarkerHologramWrapper {
	private Hologram hologram;
	private int timeToLive;

	private double ySpeed;

	public HitmarkerHologramWrapper(Hologram hologram, int timeToLive, double ySpeed) {
		this.hologram = hologram;
		this.timeToLive = timeToLive;
		this.ySpeed = ySpeed;
	}

	public void destroy() {
		hologram.delete();
	}

	public boolean isRemoved() {
		return hologram.isDeleted();
	}

	public void tick() {
		timeToLive--;
		if (timeToLive > 0) {
			timeToLive--;

			hologram.teleport(hologram.getLocation().clone().add(0D, ySpeed, 0D));
		} else {
			this.destroy();
		}
	}
}