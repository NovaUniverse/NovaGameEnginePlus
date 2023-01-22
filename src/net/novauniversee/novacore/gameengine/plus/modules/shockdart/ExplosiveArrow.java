package net.novauniversee.novacore.gameengine.plus.modules.shockdart;

import org.bukkit.event.Listener;
import net.novauniversee.novacore.gameengine.plus.NovaGameEnginePlus;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.explosivearrow.ExplosiveArrowItem;
import net.novauniversee.novacore.gameengine.plus.modules.customarrowmanager.CustomArrowManager;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.particle.NovaDustOptions;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.annotations.NovaAutoLoad;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;

@NovaAutoLoad(shouldEnable = true)
public class ExplosiveArrow extends NovaModule implements Listener {
	private Task particleTask;

	public ExplosiveArrow() {
		super("NovaGameEnginePlus.ExplosiveArrow");

		this.particleTask = new SimpleTask(NovaGameEnginePlus.getInstance(), () -> {
			CustomArrowManager.getInstance().getWrappedCustomArrows().stream().filter(wca -> (wca.getType() instanceof ExplosiveArrowItem)).forEach(wca -> {
				NovaCore.getInstance().getNovaParticleProvider().showRedstoneParticle(wca.getArrow().getLocation(), NovaDustOptions.RED);
			});
		}, 4L);
	}

	@Override
	public void onEnable() throws Exception {
		Task.tryStartTask(particleTask);
	}

	@Override
	public void onDisable() throws Exception {
		Task.tryStopTask(particleTask);
	}
}