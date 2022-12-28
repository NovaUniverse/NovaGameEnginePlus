package net.novauniversee.novacore.gameengine.plus.customitem.revivecrystal;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import net.novauniversee.novacore.gameengine.plus.NovaGameEnginePlus;
import net.novauniversee.novacore.gameengine.plus.modules.revivecrystal.ReviveCrystalManager;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItem;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class ReviveCrystalItem extends CustomItem {
	// net.novauniversee.novacore.gameengine.plus.customitem.revivecrystal.ReviveCrystalItem
	public static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYyMDA3NjQwOTI1NSwKICAicHJvZmlsZUlkIiA6ICI0NWY3YTJlNjE3ODE0YjJjODAwODM5MmRmN2IzNWY0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfSnVzdERvSXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTUwNjQ5NjI2YzQxMDEzNTJjNTk5NWM1M2I0OGJmZjYwYTkzODIxMmI3Y2U5MDI0MTVmZWI3NmVhMjczYjM1ZiIKICAgIH0KICB9Cn0";

	@Override
	protected ItemStack createItemStack(Player player) {
		ItemBuilder builder;

		if (NovaGameEnginePlus.getInstance().getItemsAdderConfig().isEnabled() && NovaGameEnginePlus.getInstance().getItemsAdderConfig().hasReviveCrystalItem()) {
			builder = ItemBuilder.fromItemsAdderNamespace(NovaGameEnginePlus.getInstance().getItemsAdderConfig().getReviveCrystalItem());
		} else {
			builder = ItemBuilder.getPlayerSkullWithBase64TextureAsBuilder(ReviveCrystalItem.TEXTURE);
		}

		builder.setName(ChatColor.AQUA + "Revive Crystal");

		builder.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		builder.addEnchant(Enchantment.DURABILITY, 1, true);

		builder.addLore("Right click at the ground to revive");
		builder.addLore("a player from your team");
		builder.addLore("");
		builder.addLore("Doing this will start a timer and spawns");
		builder.addLore("a beam that slowly descends. When it reaches");
		builder.addLore("the ground the player you selected will");
		builder.addLore("be respawned. Hovever if someone blocks the");
		builder.addLore("beam or kills the armor stand the revive");
		builder.addLore("will fail");

		return builder.build();
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		event.setCancelled(true);
		if (GameManager.getInstance().hasGame()) {
			if (GameManager.getInstance().getActiveGame().hasStarted()) {
				if (TeamManager.hasTeamManager()) {
					Team team = TeamManager.getTeamManager().getPlayerTeam(event.getPlayer());
					if (team != null) {
						if (VersionIndependentUtils.get().isInteractEventMainHand(event)) {
							if (event.getAction() == Action.RIGHT_CLICK_AIR) {
								event.getPlayer().sendMessage(ChatColor.GREEN + "Right click on the ground to revive a team member");
							} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
								Block block = event.getClickedBlock();

								if (!block.getType().isSolid()) {
									event.getPlayer().sendMessage(ChatColor.RED + "You can only place the revive crystal on solid blocks");
									return;
								}

								ReviveCrystalManager.getInstance().showUI(event.getPlayer(), block.getLocation().clone().add(0D, 1D, 0D));
							}
						}
					}
				}
			}
		}
	}
}