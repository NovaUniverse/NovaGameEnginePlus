package net.novauniversee.novacore.gameengine.plus.modules.revivecrystal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import net.novauniversee.novacore.gameengine.plus.customitem.revivecrystal.ReviveCrystalItem;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.ColoredBlockType;
import net.zeeraa.novacore.spigot.abstraction.enums.NovaCoreGameVersion;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.annotations.NovaAutoLoad;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.module.modules.gui.GUIAction;
import net.zeeraa.novacore.spigot.module.modules.gui.callbacks.GUIClickCallback;
import net.zeeraa.novacore.spigot.module.modules.gui.holders.GUIReadOnlyHolder;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

@NovaAutoLoad(shouldEnable = true)
public class ReviveCrystalManager extends NovaModule implements Listener {
	private static ReviveCrystalManager instance;

	private Map<UUID, Location> reviveLocations;
	private List<ReviveCrystalEffect> effects;
	private Task task;
	private int respawnTimeTicks;

	public static ReviveCrystalManager getInstance() {
		return instance;
	}

	public ReviveCrystalManager() {
		super("NovaGameEnginePlus.ReviveCrystalManager");
	}

	public void showUI(Player player, Location location) {
		if (location.getBlockY() < VersionIndependentUtils.get().getMinY() + 4) {
			player.sendMessage(ChatColor.RED + "You need to place the crystal higher up in");
			return;
		}

		if (!isLocationOk(location)) {
			player.sendMessage(ChatColor.RED + "Cant start revive since there is no clear opening to the sky");
			return;
		}

		Team team = TeamManager.getTeamManager().getPlayerTeam(player);
		if (team != null) {
			List<UUID> potentialPlayers = new ArrayList<>();

			team.getMembers().stream().filter(uuid -> !GameManager.getInstance().getActiveGame().getPlayers().contains(uuid) && !uuid.equals(player.getUniqueId())).forEach(uuid -> {
				Player p2 = Bukkit.getServer().getPlayer(uuid);
				if (p2 != null) {
					potentialPlayers.add(p2.getUniqueId());
				}
			});

			if (potentialPlayers.size() == 0) {
				player.sendMessage(ChatColor.RED + "No players online to revive");
				return;
			}

			GUIReadOnlyHolder holder = new GUIReadOnlyHolder();
			Inventory inventory = Bukkit.createInventory(holder, 9 * 3, "Select player to revive");

			ItemStack bg = new ItemBuilder(VersionIndependentUtils.get().getColoredItem(DyeColor.WHITE, ColoredBlockType.GLASS_PANE)).setAmount(1).setName(" ").build();
			for (int i = 0; i < inventory.getSize(); i++) {
				inventory.setItem(i, bg.clone());
			}

			int slot = 0;
			for (UUID uuid : potentialPlayers) {
				holder.addClickCallback(slot, new GUIClickCallback() {
					@Override
					public GUIAction onClick(Inventory clickedInventory, Inventory inventory, HumanEntity entity, int clickedSlot, SlotType slotType, InventoryAction clickType) {
						try {
							Player p2 = Bukkit.getServer().getPlayer(uuid);
							if (p2 != null) {
								beginRevive(player, uuid);
							} else {
								player.sendMessage(ChatColor.RED + "That player is not online");
							}
						} catch (Exception e) {
							Log.error("ReviveCrystalManager", "Failed to begin revive. " + e.getClass().getName() + " " + e.getMessage());
							e.printStackTrace();
						}
						return GUIAction.CANCEL_INTERACTION;
					}
				});

				Player p2 = Bukkit.getServer().getPlayer(uuid);
				ItemBuilder builder = ItemBuilder.playerSkull(p2);

				builder.setName(ChatColor.GREEN + "Revive " + p2.getName());
				builder.setAmount(1);
				builder.addLore("Click to revice " + player.getName() + ". You need to defend");
				builder.addLore("the revive beam until it reaches the ground");

				inventory.setItem(slot, builder.build());

				slot++;
			}

			reviveLocations.put(player.getUniqueId(), location);

			player.openInventory(inventory);
		}
	}

	public void beginRevive(Player initiator, UUID uuid) {
		Location location = reviveLocations.get(initiator.getUniqueId());
		if (!isLocationOk(location)) {
			initiator.sendMessage(ChatColor.RED + "Cant start revive since there is no clear opening to the sky");
			return;
		}

		Player player = Bukkit.getServer().getPlayer(uuid);

		ReviveCrystalEffect existingEffect = effects.stream().filter(ef -> ef.getPlayer().equals(player)).findFirst().orElse(null);
		if (existingEffect != null) {
			initiator.sendMessage(ChatColor.RED + "That player is already being revived");
			return;
		}

		ItemStack item = VersionIndependentUtils.get().getItemInMainHand(initiator);
		if (item == null) {
			return;
		}

		if (!CustomItemManager.getInstance().isCustomItem(item)) {
			return;
		}

		if (!CustomItemManager.getInstance().isType(item, ReviveCrystalItem.class)) {
			return;
		}

		if (item.getAmount() > 1) {
			item.setAmount(item.getAmount() - 1);
		} else {
			VersionIndependentUtils.get().setItemInMainHand(initiator, ItemBuilder.AIR);
		}

		Team team = TeamManager.getTeamManager().getPlayerTeam(player);
		effects.add(new ReviveCrystalEffect(location, player, team));
	}

	public static final String[] ALLOWED_BLOCKS = { "GLASS", "STAINED_GLASS" };

	public static boolean isLocationOk(Location location) {
		for (int y = location.getBlockY(); y < location.getWorld().getMaxHeight(); y++) {
			Location l2 = location.clone();
			l2.setY(y);

			Material material = l2.getBlock().getType();

			if (material == Material.AIR) {
				continue;
			}

			String materialName = material.name();

			if (materialName.contains("GLASS")) {
				continue;
			}

			return false;
		}

		return true;
	}

	public List<ReviveCrystalEffect> getEffects() {
		return effects;
	}

	public int getRespawnTimeTicks() {
		return respawnTimeTicks;
	}

	public void setRespawnTimeTicks(int respawnTimeTicks) {
		this.respawnTimeTicks = respawnTimeTicks;
	}

	@Override
	public void onLoad() {
		ReviveCrystalManager.instance = this;

		respawnTimeTicks = 1200;
		reviveLocations = new HashMap<UUID, Location>();
		effects = new ArrayList<ReviveCrystalEffect>();
		task = new SimpleTask(getPlugin(), new Runnable() {
			@Override
			public void run() {
				effects.forEach(e -> e.tick());
				effects.removeIf(e -> e.shouldRemove());
			}
		}, 0L);
	}

	public static final void registerRecipe() {
		ItemStack result = CustomItemManager.getInstance().getCustomItemStack(ReviveCrystalItem.class, null);

		ShapedRecipe recipe1 = new ShapedRecipe(result);
		ShapedRecipe recipe2 = new ShapedRecipe(result);

		recipe1.shape(" L ", "DHD", " L ");
		recipe2.shape(" D ", "LHL", " D ");

		VersionIndependentUtils.get().setShapedRecipeIngredientAsPlayerSkull(recipe1, 'H');
		VersionIndependentUtils.get().setShapedRecipeIngredientAsPlayerSkull(recipe2, 'H');

		VersionIndependentUtils.get().setShapedRecipeIngredientAsDye(recipe1, 'L', DyeColor.BLUE);
		VersionIndependentUtils.get().setShapedRecipeIngredientAsDye(recipe2, 'L', DyeColor.BLUE);

		recipe1.setIngredient('D', Material.DIAMOND);
		recipe2.setIngredient('D', Material.DIAMOND);

		Bukkit.getServer().addRecipe(recipe1);
		Bukkit.getServer().addRecipe(recipe2);
	}

	@Override
	public void onEnable() throws Exception {
		Task.tryStartTask(task);
	}

	@Override
	public void onDisable() throws Exception {
		Task.tryStopTask(task);
		effects.forEach(e -> e.remove());
		effects.clear();
		reviveLocations.clear();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.ENTITY_EXPLOSION) {
			if (VersionIndependentUtils.get().getNovaCoreGameVersion() == NovaCoreGameVersion.V_1_8) {
				return;
			}

			this.effects.stream().filter(ef -> ef.getWorld().equals(e.getEntity().getWorld())).forEach(effect -> {
				if (e.getEntity().getLocation().distance(effect.getFireworkLocation()) < 7) {
					e.setCancelled(true);
				}
			});
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		ReviveCrystalEffect effect = effects.stream().filter(ef -> ef.getPlayer().equals(player)).findFirst().orElse(null);
		if (effect != null) {
			effect.fail();
			effect.getTeam().sendMessage(ChatColor.RED + "Revive failed since " + player.getName() + " disconnected");
			effects.remove(effect);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onEntityDamage(EntityDeathEvent e) {
		if (e.getEntity() instanceof ArmorStand) {
			if (effects.stream().filter(effect -> effect.getArmorStand().equals(e.getEntity())).count() > 0) {
				Log.trace("ReviveCrystal", "Preventing armor stand interact event");
				e.getDrops().clear();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof ArmorStand) {
			if (effects.stream().filter(effect -> effect.getArmorStand().equals(e.getRightClicked())).count() > 0) {
				Log.trace("ReviveCrystal", "Preventing armor stand from dropping items");
				e.setCancelled(true);
			}
		}
	}
}