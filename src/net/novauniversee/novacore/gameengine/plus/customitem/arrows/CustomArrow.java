package net.novauniversee.novacore.gameengine.plus.customitem.arrows;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItem;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public abstract class CustomArrow extends CustomItem {
	@Override
	protected ItemStack createItemStack(@Nullable Player player) {
		ItemBuilder builder = this.initItemBuilder();

		builder.setName(ChatColor.RED + "ERR:NAME_NOT_SET");
		builder.setAmount(1);

		if (this.addEnchantedEffect()) {
			builder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			builder.addEnchant(Enchantment.DURABILITY, 1, true);
		}

		ItemStack item = builder.build();

		this.addItemStackAttributes(item, player);

		return item;
	}

	public ItemBuilder initItemBuilder() {
		return new ItemBuilder(Material.ARROW);
	}

	protected boolean addEnchantedEffect() {
		return true;
	}

	protected abstract void addItemStackAttributes(ItemStack arrow, @Nullable Player player);

	public void onFire(Player shooter) {
	}

	public void onHitEntity(Arrow arrow, Entity entity, Player shooter) {
	}

	public void onHitAnything(Arrow arrow, Location hitLocation, Player shooter) {
	}
}