package net.novauniversee.novacore.gameengine.plus.modules.customarrowmanager;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import net.novauniversee.novacore.gameengine.plus.customitem.arrows.CustomArrow;

public class WrappedCustomArrow {
	private Arrow arrow;
	private CustomArrow type;
	private Player shooter;

	public WrappedCustomArrow(Arrow arrow, CustomArrow type, Player shooter) {
		this.arrow = arrow;
		this.type = type;
		this.shooter = shooter;
	}

	public Arrow getArrow() {
		return arrow;
	}

	public CustomArrow getType() {
		return type;
	}

	public Player getShooter() {
		return shooter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WrappedCustomArrow) {
			WrappedCustomArrow o2 = (WrappedCustomArrow) obj;

			return o2.getArrow().getUniqueId().equals(this.getArrow().getUniqueId());
		}

		return super.equals(obj);
	}
}