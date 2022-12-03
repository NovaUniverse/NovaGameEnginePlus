package net.novauniversee.novacore.gameengine.plus.customitem.arrows.explosivearrow;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.CustomArrow;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class ExplosiveArrowItem extends CustomArrow {
	@Override
	protected void addItemStackAttributes(ItemStack arrow, Player player) {
		ItemMeta meta = arrow.getItemMeta();

		meta.setDisplayName(ChatColor.RED + "Explosive Arrow");
		meta.setLore(ItemBuilder.generateLoreList(ChatColor.RED + "Creates a small explosion on hit"));

		arrow.setItemMeta(meta);
	}

	@Override
	public void onHitAnything(Arrow arrow, Location hitLocation, Player shooter) {
		Log.trace("ExplosiveArrow", "onHitAnything() location: " + arrow.getLocation() + " shooter: " + shooter.toString());

		arrow.getLocation().getWorld().createExplosion(arrow.getLocation(), 1.5F, true);
	}
}