package net.novauniversee.novacore.gameengine.plus.customitem.smokebomb;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependentSound;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItem;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class SmokeGrenade extends CustomItem {
	// net.novauniversee.novacore.gameengine.plus.customitem.smokebomb.SmokeGrenade
	public static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYzODAyMDQxNzgzNSwKICAicHJvZmlsZUlkIiA6ICJhMjk1ODZmYmU1ZDk0Nzk2OWZjOGQ4ZGE0NzlhNDNlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaWVydGVsdG9hc3RpaWUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTUzZTViYWRiMmM3MzRjMWUyMmRkMGY0M2JhMmE5NmQyM2VkYzJjZDE2ZmRhMjEwOTViMTdmYjA4Y2ZjZjY0ZCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
	public static final int DURATION = 5;
	public static final double RADIUS = 4;
	public static final int PARTICLE_COUNT = 300;

	private Random random;

	public SmokeGrenade() {
		this.random = new Random();
	}

	@Override
	protected ItemStack createItemStack(Player player) {
		ItemBuilder builder = new ItemBuilder(ItemBuilder.getPlayerSkullWithBase64Texture(SmokeGrenade.TEXTURE));

		builder.setName(ChatColor.GREEN + "Smoke Grenade");
		builder.setAmount(1);
		builder.addLore("Right click to temporary blind players");
		builder.addLore("in a " + ((int) SmokeGrenade.RADIUS) + " block radius");

		return builder.build();
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (VersionIndependentUtils.get().isInteractEventMainHand(event)) {
			Location location = event.getPlayer().getLocation();
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				location = event.getClickedBlock().getLocation().clone().add(0D, 1D, 0D);
			}

			ItemStack item = VersionIndependentUtils.get().getItemInMainHand(event.getPlayer());
			if (item == null) {
				return;
			}

			if (!CustomItemManager.getInstance().isCustomItem(item)) {
				return;
			}

			if (!CustomItemManager.getInstance().isType(item, SmokeGrenade.class)) {
				return;
			}

			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() - 1);
			} else {
				VersionIndependentUtils.get().setItemInMainHand(event.getPlayer(), ItemBuilder.AIR);
			}

			final Location finalLocation = location;

			for (int i = 0; i < PARTICLE_COUNT; i++) {
				double xOffset = ((random.nextDouble() * 2) - 1D) * RADIUS;
				double yOffset = random.nextDouble() * RADIUS;
				double zOffset = ((random.nextDouble() * 2) - 1D) * RADIUS;

				ParticleEffect.SMOKE_LARGE.display(location.clone().add(xOffset, yOffset, zOffset));
			}

			VersionIndependentSound.FIZZ.playAtLocation(finalLocation, 1F, 0.5F);

			Bukkit.getServer().getOnlinePlayers().stream().filter(p -> p.getWorld().equals(finalLocation.getWorld())).filter(p -> p.getLocation().distance(finalLocation) < RADIUS).forEach(player -> {
				player.sendMessage(ChatColor.GRAY + "Blinded by smoke grenade");
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * DURATION, 0));
			});
		}
	}
}