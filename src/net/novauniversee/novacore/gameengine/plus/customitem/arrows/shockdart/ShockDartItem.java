package net.novauniversee.novacore.gameengine.plus.customitem.arrows.shockdart;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.novauniversee.novacore.gameengine.plus.NovaGameEnginePlus;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.CustomArrow;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.utils.RandomGenerator;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependentSound;
import net.zeeraa.novacore.spigot.abstraction.particle.NovaDustOptions;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class ShockDartItem extends CustomArrow {
	private Random random;

	public ShockDartItem() {
		this.random = new Random();
	}

	@Override
	protected void addItemStackAttributes(ItemStack arrow, Player player) {
		ItemMeta meta = arrow.getItemMeta();

		meta.setDisplayName(ChatColor.BLUE + "Shock dart");
		meta.setLore(ItemBuilder.generateLoreList(ChatColor.BLUE + "Deals damage to players in a 2 block radius"));

		arrow.setItemMeta(meta);
	}

	@Override
	public void onHitAnything(Arrow arrow, Location hitLocation, Player shooter) {
		Log.trace("ShockDart", "onHitAnything() location: " + arrow.getLocation() + " shooter: " + shooter.toString());

		for (int i = 0; i < 40; i++) {
			double x = RandomGenerator.generateDouble(-2.5D, 2.5D, random);
			double y = RandomGenerator.generateDouble(-2.5D, 2.5D, random);
			double z = RandomGenerator.generateDouble(-2.5D, 2.5D, random);

			Location particleLocation = arrow.getLocation().clone().add(x, y, z);
			NovaCore.getInstance().getNovaParticleProvider().showRedstoneParticle(particleLocation, NovaDustOptions.BLUE);

			VersionIndependentSound.FIZZ.playAtLocation(particleLocation);
		}

		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			if (player.getLocation().getWorld().equals(arrow.getLocation().getWorld())) {
				double distance = player.getLocation().distance(arrow.getLocation());
				if (distance < 4) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, false, false), true);
					Log.trace("ShockDart", "Inflicting damage and slowness to " + player.getName() + " since they are in the shock radius. dist: " + distance);
					player.damage(4D);
				} else {
					// Log.trace("ShockDart", player.getName() + " is outside the radius");
				}
			}
		});
	}

	@Override
	public void onHitEntity(Arrow arrow, Entity entity, Player shooter) {
		Log.trace("ShockDart", "onHitEntity() entity: " + entity.toString() + " shooter: " + shooter.toString());
		if (entity instanceof LivingEntity) {
			Log.trace("ShockDart", "Direct hit on entity " + entity.getName());
			((LivingEntity) entity).damage(2D);
			((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0, false, false), true);
		}
	}

	@Override
	public ItemBuilder initItemBuilder() {
		if (NovaGameEnginePlus.getInstance().getItemsAdderConfig().isEnabled() && NovaGameEnginePlus.getInstance().getItemsAdderConfig().hasShockArrowItem()) {
			return ItemBuilder.fromItemsAdderNamespace(NovaGameEnginePlus.getInstance().getItemsAdderConfig().getShockArrowItem());
		} else {
			return super.initItemBuilder();
		}
	}
}