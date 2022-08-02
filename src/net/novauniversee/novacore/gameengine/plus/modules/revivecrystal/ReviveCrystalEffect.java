package net.novauniversee.novacore.gameengine.plus.modules.revivecrystal;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.meta.FireworkMeta;

import net.novauniversee.novacore.gameengine.plus.NovaGameEnginePlus;
import net.novauniversee.novacore.gameengine.plus.customitem.revivecrystal.ReviveCrystalItem;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.ColoredBlockType;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependentSound;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.module.modules.lootdrop.particles.LootdropParticleEffect;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;
import net.zeeraa.novacore.spigot.utils.LocationUtils;

public class ReviveCrystalEffect {
	public static final float ARMOR_STAND_ROTATION_SPEED = 2;

	private Location location;
	private Player player;
	private Team team;

	private int ticksLeft;

	private ArmorStand armorStand;
	private float armorStandYaw;

	private Map<Location, Material> toRestore;

	private LootdropParticleEffect particleEffect;

	private Location fireworkLocation;

	private boolean done;
	private int removeTimer;

	public ReviveCrystalEffect(Location location, Player player, Team team) {
		this.location = LocationUtils.centerLocation(location);
		this.player = player;
		this.team = team;

		this.toRestore = new HashMap<>();

		this.ticksLeft = ReviveCrystalManager.getInstance().getRespawnTimeTicks();

		this.fireworkLocation = this.location;

		this.armorStand = (ArmorStand) location.getWorld().spawnEntity(this.location.clone().add(0D, -1D, 0D), EntityType.ARMOR_STAND);
		this.armorStand.setHelmet(ItemBuilder.getPlayerSkullWithBase64Texture(ReviveCrystalItem.TEXTURE));
		this.armorStand.setCustomNameVisible(true);
		this.armorStand.setCustomName(ChatColor.GREEN + "Reviving " + player.getName() + "...");
		this.armorStand.setGravity(false);
		this.armorStand.setBasePlate(false);

		this.armorStandYaw = 0;

		this.removeTimer = 20;

		this.done = false;

		Log.trace(location.toString());

		this.particleEffect = new LootdropParticleEffect(this.location.clone().add(0D, 1.5D, 0D));

		VersionIndependentSound.NOTE_PLING.broadcast();
		Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + team.getDisplayName() + " is respawning " + player.getName() + " at X: " + location.getBlockX() + " Y: " + location.getBlockZ());

		this.placeStructure();
	}

	private void placeStructure() {
		for (int x = (int) location.getBlockX() - 1; x <= location.getBlockX() + 1; x++) {
			for (int z = location.getBlockZ() - 1; z <= location.getBlockZ() + 1; z++) {
				Location l1 = new Location(location.getWorld(), x, location.getBlockY() - 3, z);
				Location l2 = new Location(location.getWorld(), x, location.getBlockY() - 2, z);

				toRestore.put(l1, l1.getWorld().getBlockAt(l1).getType());
				toRestore.put(l2, l2.getWorld().getBlockAt(l2).getType());

				l1.getBlock().setType(Material.IRON_BLOCK);
				l2.getBlock().setType(Material.IRON_BLOCK);
			}
		}

		Location beaconLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 2, location.getBlockZ());
		Location glassLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());

		toRestore.put(glassLocation, beaconLocation.getWorld().getBlockAt(glassLocation).getType());

		beaconLocation.getBlock().setType(Material.BEACON);
		NovaCore.getInstance().getVersionIndependentUtils().setColoredBlock(glassLocation.getBlock(), DyeColor.BLUE, ColoredBlockType.GLASS_BLOCK);
	}

	public Location getLocation() {
		return location;
	}

	public Player getPlayer() {
		return player;
	}

	public Team getTeam() {
		return team;
	}

	public ArmorStand getArmorStand() {
		return armorStand;
	}

	public boolean isDone() {
		return done;
	}

	public void fail() {
		if (done) {
			return;
		}

		getTeam().getOnlinePlayers().forEach(p -> VersionIndependentSound.WITHER_HURT.play(player));
		VersionIndependentUtils.get().sendTitle(player, "", ChatColor.RED + "Respawning failed", 0, 40, 20);
		this.remove();
	}

	public void remove() {
		if (done) {
			return;
		}

		armorStand.remove();
		toRestore.forEach((location, material) -> location.getBlock().setType(material));

		this.done = true;
	}

	public boolean shouldRemove() {
		return removeTimer == 0;
	}

	public void tick() {
		if (done) {
			if (removeTimer > 0) {
				removeTimer--;
			}
			return;
		}

		// only check 2 times / tick
		if (ticksLeft % 10 == 0) {
			if (!ReviveCrystalManager.isLocationOk(location)) {
				team.sendMessage(ChatColor.RED + "Failed to protect revive beam for " + player.getName());
				fail();
				return;
			}
		}

		if (armorStand.isDead()) {
			team.sendMessage(ChatColor.RED + "Failed to protect revive crystal for " + player.getName() + ". Revive canceled");
			fail();
			return;
		}

		// Particles
		particleEffect.update();

		// Rotate armor stand
		Location newLocation = location.clone().add(0D, -1D, 0D);
		newLocation.setYaw(armorStandYaw);
		armorStandYaw += ARMOR_STAND_ROTATION_SPEED;
		if (armorStandYaw > 180) {
			armorStandYaw -= 360;
		}
		armorStand.teleport(newLocation, TeleportCause.PLUGIN);

		// Decrement time
		ticksLeft--;

		if (ticksLeft % 10 == 0) {
			String timeText = TextUtils.formatTimeToHMS((int) Math.floor(ticksLeft / 20D));
			VersionIndependentUtils.get().sendTitle(player, "", ChatColor.GREEN + "Respawning in " + timeText, 0, 20, 10);
		}

		if (ticksLeft <= 0) {
			Location respawnLocation = location.clone().add(0D, 1D, 0D);
			fireworkLocation = respawnLocation;
			Firework fw = (Firework) this.location.getWorld().spawnEntity(respawnLocation, EntityType.FIREWORK);
			FireworkMeta fwm = fw.getFireworkMeta();

			fwm.setPower(1);
			fwm.addEffect(FireworkEffect.builder().with(Type.BALL_LARGE).withColor(Color.BLUE).trail(true).flicker(true).build());

			fw.setFireworkMeta(fwm);

			Bukkit.getScheduler().scheduleSyncDelayedTask(NovaCore.getInstance(), new Runnable() {
				@Override
				public void run() {
					fw.detonate();
				}
			}, 2L);

			GameManager.getInstance().getActiveGame().addPlayer(player);
			player.teleport(respawnLocation);
			player.setGameMode(GameMode.SURVIVAL);
			VersionIndependentUtils.get().sendTitle(player, ChatColor.GREEN + "Respawned", "", 0, 40, 20);
			team.sendMessage(ChatColor.GREEN + player.getName() + " was respawned");
			team.getOnlinePlayers().forEach(p -> VersionIndependentSound.NOTE_PLING.play(p));

			Event event = new ReviveCrystalRespawnPlayerEvent(player);
			Bukkit.getServer().getPluginManager().callEvent(event);

			this.remove();
			return;
		} else {
			// Firework
			if (ticksLeft % 4 == 0) {
				double progress = ((double) ticksLeft) / ((double) ReviveCrystalManager.getInstance().getRespawnTimeTicks());
				double y = location.getBlockY();
				double maxHeight = location.getWorld().getMaxHeight();
				double dist = maxHeight - y;
				double offset = dist * progress;

				// Log.info("Revive Debug", "progress: " + progress + " y: " + y + " maxHeigth:
				// " + maxHeight + " dist: " + dist + " offset: " + offset);

				fireworkLocation = location.clone().add(0D, offset, 0D);

				Firework fw = (Firework) fireworkLocation.getWorld().spawnEntity(fireworkLocation, EntityType.FIREWORK);
				FireworkMeta fwm = fw.getFireworkMeta();

				fwm.setPower(1);
				fwm.addEffect(FireworkEffect.builder().withColor(Color.BLUE).trail(true).build());

				fw.setFireworkMeta(fwm);

				Bukkit.getScheduler().scheduleSyncDelayedTask(NovaGameEnginePlus.getInstance(), new Runnable() {
					@Override
					public void run() {
						fw.detonate();
					}
				}, 2L);
			}
		}
	}

	public World getWorld() {
		return location.getWorld();
	}

	public Location getFireworkLocation() {
		return fireworkLocation;
	}
}