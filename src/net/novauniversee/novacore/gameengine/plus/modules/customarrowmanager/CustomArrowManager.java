package net.novauniversee.novacore.gameengine.plus.modules.customarrowmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.novauniversee.novacore.gameengine.plus.NovaGameEnginePlus;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.CustomArrow;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.annotations.NovaAutoLoad;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItem;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;

@NovaAutoLoad(shouldEnable = true)
public class CustomArrowManager extends NovaModule implements Listener {
	private static CustomArrowManager instance;

	private List<WrappedCustomArrow> wrappedCustomArrows;
	private Map<Player, Map<String, Integer>> playerArrowCount;

	private Task cleanupTask;

	public static CustomArrowManager getInstance() {
		return instance;
	}

	public CustomArrowManager() {
		super("NovaGameEnginePlus.CustomArrowManager");

		CustomArrowManager.instance = this;

		this.playerArrowCount = new HashMap<Player, Map<String, Integer>>();
		this.wrappedCustomArrows = new ArrayList<>();

		this.cleanupTask = new SimpleTask(NovaGameEnginePlus.getInstance(), () -> {
			wrappedCustomArrows.removeIf(wa -> wa.getArrow().isDead());
		}, 10L);
	}

	@Override
	public void onEnable() throws Exception {
		Bukkit.getServer().getOnlinePlayers().forEach(p -> {
			setupPlayer(p);
		});

		Task.tryStartTask(cleanupTask);
	}

	@Override
	public void onDisable() throws Exception {
		playerArrowCount.clear();
		wrappedCustomArrows.clear();

		Task.tryStopTask(cleanupTask);
	}

	public List<WrappedCustomArrow> getWrappedCustomArrows() {
		return wrappedCustomArrows;
	}

	public void setupPlayer(Player player) {
		playerArrowCount.put(player, new HashMap<>());
		updatePlayerArrowCount(player);
	}

	public Map<String, Integer> getArrowCount(Player player) {
		Map<String, Integer> result = new HashMap<String, Integer>();

		for (ItemStack item : player.getInventory().getContents()) {
			if (CustomItemManager.getInstance().isCustomItem(item)) {
				CustomItem customItem = CustomItemManager.getInstance().getCustomItem(item);
				if (customItem != null) {
					if (customItem instanceof CustomArrow) {
						if (result.containsKey(customItem.getClass().getName())) {
							int oldVal = result.get(customItem.getClass().getName());
							result.put(customItem.getClass().getName(), oldVal + item.getAmount());
						} else {
							result.put(customItem.getClass().getName(), item.getAmount());
						}
					}
				}
			}
		}

		return result;
	}

	public void updatePlayerArrowCount(Player player) {
		playerArrowCount.put(player, this.getArrowCount(player));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		playerArrowCount.remove(e.getPlayer());
	}

	public String getArrowCountDifferential(Map<String, Integer> before, Map<String, Integer> after) {
		for (String key : before.keySet()) {
			if (!after.containsKey(key)) {
				return key;
			}

			int beforeValue = before.get(key);
			int afterValue = after.get(key);

			if (beforeValue != afterValue) {
				return key;
			}
		}

		return null;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player) {
			final Player player = (Player) e.getEntity();
			if (playerArrowCount.containsKey(player)) {

				if (e.getProjectile() instanceof Arrow) {
					final Arrow arrow = (Arrow) e.getProjectile();

					new BukkitRunnable() {
						@Override
						public void run() {
							Map<String, Integer> newArrowCount = getArrowCount(player);

							String customItemUsed = getArrowCountDifferential(playerArrowCount.get(player), newArrowCount);
							if (customItemUsed != null) {
								CustomArrow type = (CustomArrow) CustomItemManager.getInstance().getCustomItem(customItemUsed);

								Log.trace("CustomArrowManager", player.getName() + " fired arrow " + arrow.getUniqueId() + " of custom type " + type.getClass().getName());
								wrappedCustomArrows.add(new WrappedCustomArrow(arrow, type, player));
							}
							playerArrowCount.put(player, newArrowCount);
						}
					}.runTaskLater(NovaGameEnginePlus.getInstance(), 1L);
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Arrow) {
			if (wrappedCustomArrows.stream().anyMatch(wa -> wa.getArrow().getUniqueId().equals(e.getDamager().getUniqueId()))) {
				WrappedCustomArrow wca = wrappedCustomArrows.stream().filter(wa -> wa.getArrow().getUniqueId().equals(e.getDamager().getUniqueId())).findFirst().orElse(null);

				CustomArrow customArrow = wca.getType();
				Player shooter = wca.getShooter();

				try {
					customArrow.onHitEntity(wca.getArrow(), e.getEntity(), shooter);
				} catch (Exception ex) {
					ex.printStackTrace();
					Log.error("CustomArrowManager", "Exception occured while processing hit event of entity type. " + ex.getClass().getName() + " " + ex.getMessage());
				}

				new BukkitRunnable() {
					@Override
					public void run() {
						wrappedCustomArrows.removeIf(wa -> wa.equals(wca));
					}
				}.runTaskLater(NovaGameEnginePlus.getInstance(), 1L);
			}
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		Log.trace(e.toString());
		if (wrappedCustomArrows.stream().anyMatch(wa -> wa.getArrow().getUniqueId().equals(e.getEntity().getUniqueId()))) {
			WrappedCustomArrow wca = wrappedCustomArrows.stream().filter(wa -> wa.getArrow().getUniqueId().equals(e.getEntity().getUniqueId())).findFirst().orElse(null);

			Block hit = VersionIndependentUtils.get().getBlockFromProjectileHitEvent(e);
			Location hitLocation = hit.getLocation();
			Player shooter = wca.getShooter();
			CustomArrow customArrow = wca.getType();

			try {
				customArrow.onHitAnything(wca.getArrow(), hitLocation, shooter);
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.error("CustomArrowManager", "Exception occured while processing hit event of block type. " + ex.getClass().getName() + " " + ex.getMessage());
			}

			new BukkitRunnable() {
				@Override
				public void run() {
					wrappedCustomArrows.removeIf(wa -> wa.equals(wca));
				}
			}.runTaskLater(NovaGameEnginePlus.getInstance(), 1L);
		}
	}

	// ==== Arrow count update events =====
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		setupPlayer(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updatePlayerArrowCount(e.getPlayer());
			}
		}.runTaskLater(NovaGameEnginePlus.getInstance(), 0L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerRespawnEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updatePlayerArrowCount(e.getPlayer());
			}
		}.runTaskLater(NovaGameEnginePlus.getInstance(), 0L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updatePlayerArrowCount(e.getPlayer());
			}
		}.runTaskLater(NovaGameEnginePlus.getInstance(), 0L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupItem(InventoryClickEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updatePlayerArrowCount((Player) e.getWhoClicked());
			}
		}.runTaskLater(NovaGameEnginePlus.getInstance(), 0L);
	}
}