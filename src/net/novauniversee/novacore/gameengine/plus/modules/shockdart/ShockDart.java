package net.novauniversee.novacore.gameengine.plus.modules.shockdart;

import java.awt.Color;

import org.bukkit.event.Listener;
import net.novauniversee.novacore.gameengine.plus.NovaGameEnginePlus;
import net.novauniversee.novacore.gameengine.plus.customitem.arrows.shockdart.ShockDartItem;
import net.novauniversee.novacore.gameengine.plus.modules.customarrowmanager.CustomArrowManager;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.module.NovaModule;
import net.zeeraa.novacore.spigot.module.annotations.NovaAutoLoad;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import xyz.xenondevs.particle.ParticleEffect;

@NovaAutoLoad(shouldEnable = true)
public class ShockDart extends NovaModule implements Listener {
	private Task particleTask;

	public ShockDart() {
		super("NovaGameEnginePlus.ShockDart");

		this.particleTask = new SimpleTask(NovaGameEnginePlus.getInstance(), () -> {
			CustomArrowManager.getInstance().getWrappedCustomArrows().stream().filter(wca -> (wca.getType() instanceof ShockDartItem)).forEach(wca -> {
				ParticleEffect.REDSTONE.display(wca.getArrow().getLocation(), Color.BLUE);
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