package net.novauniversee.novacore.gameengine.plus.modules.hologramhitmarkers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.annotations.NovaAutoLoad;

@NovaAutoLoad(shouldEnable = false)
public class ShowHologramHitmarkerOnDamage extends NovaModule implements Listener {

	public ShowHologramHitmarkerOnDamage() {
		super("NovaGameenginePlus.HologramHitmarkers.Auto");

		addDependency(HologramHitmarkers.class);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				Player player = (Player) e.getEntity();
				Player attacker = (Player) e.getDamager();

				double damage = e.getFinalDamage();

				if (HologramHitmarkers.getInstance().isEnabled()) {
					HologramHitmarkers.getInstance().showHitmarker(attacker, player, damage, 2, true);
				}
			}
		}
	}
}