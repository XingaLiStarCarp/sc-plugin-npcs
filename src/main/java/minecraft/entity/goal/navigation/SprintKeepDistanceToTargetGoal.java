package minecraft.entity.goal.navigation;

import net.minecraft.world.entity.Mob;

/**
 * 疾跑与目标实体保持距离
 */
public class SprintKeepDistanceToTargetGoal extends KeepDistanceToTargetGoal {
	protected double finalSpeedModifier;
	private int speedUpTicks;
	private double speedUpAcc;

	public SprintKeepDistanceToTargetGoal(Mob mob, double keepDistanceMin, double keepDistanceMax, double finalSpeedModifier, int speedUpTicks, boolean interrupt, int updateTicks) {
		super(mob, keepDistanceMin, keepDistanceMax, 1.0, interrupt, updateTicks);
		this.finalSpeedModifier = finalSpeedModifier;
		this.speedUpTicks = speedUpTicks;
		this.speedUpAcc = finalSpeedModifier / speedUpTicks;
	}

	public SprintKeepDistanceToTargetGoal(Mob mob, double keepDistanceMin, double keepDistanceMax, double finalSpeedModifier, int speedUpTicks) {
		this(mob, keepDistanceMin, keepDistanceMax, finalSpeedModifier, speedUpTicks, true, DEFAULT_UPDATE_TICKS);
	}

	public int getSpeedUpTicks() {
		return speedUpTicks;
	}

	public void setSpeedUpTicks(int speedUpTicks) {
		this.speedUpTicks = speedUpTicks;
		this.speedUpAcc = this.finalSpeedModifier / speedUpTicks;
	}

	public double getFinalSpeedModifier() {
		return finalSpeedModifier;
	}

	public void setFinalSpeedModifier(double finalSpeedModifier) {
		this.finalSpeedModifier = finalSpeedModifier;
		this.speedUpAcc = finalSpeedModifier / this.speedUpTicks;
	}

	@Override
	public void update() {
		super.update();
		if (!mob.level().isClientSide()) {
			double currentSpeedModifier = 1.0;
			if (this.getTargetTicks() < this.speedUpTicks) {
				currentSpeedModifier += this.getTargetTicks() * speedUpAcc;
			} else {
				currentSpeedModifier = this.finalSpeedModifier;
			}
			navigation.setSpeedModifier(currentSpeedModifier);
		}
	}
}
