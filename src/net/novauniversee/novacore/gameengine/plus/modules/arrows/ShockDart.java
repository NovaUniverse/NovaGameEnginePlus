package net.novauniversee.novacore.gameengine.plus.modules.arrows;

import org.bukkit.event.Listener;
import net.novauniversee.novacore.gameengine.plus.NovaGameEnginePlus;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.shockdart.ShockDartItem;
import net.novauniversee.novacore.gameengine.plus.modules.customarrowmanager.CustomArrowManager;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.particle.NovaDustOptions;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.annotations.NovaAutoLoad;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;

@NovaAutoLoad(shouldEnable = true)
public class ShockDart extends NovaModule implements Listener {
	private Task particleTask;

	public ShockDart() {
		super("NovaGameEnginePlus.ShockDart");

		this.particleTask = new SimpleTask(NovaGameEnginePlus.getInstance(), () -> {
			CustomArrowManager.getInstance().getWrappedCustomArrows().stream().filter(wca -> (wca.getType() instanceof ShockDartItem)).forEach(wca -> {
				NovaCore.getInstance().getNovaParticleProvider().showColoredRedstoneParticle(wca.getArrow().getLocation(), NovaDustOptions.BLUE);
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