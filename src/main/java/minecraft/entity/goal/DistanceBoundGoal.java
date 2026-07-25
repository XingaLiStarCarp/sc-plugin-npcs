package minecraft.entity.goal;

import javabase.Algorithms;
import net.minecraft.world.entity.Mob;

public abstract class DistanceBoundGoal extends BaseGoal {
	private double[] boundDistances;
	private double[] boundDistancesSqr;

	private double currentDistanceSqr;
	private int currentBoundLevel;

	public DistanceBoundGoal(Mob mob, int interval) {
		super(mob, interval);
	}

	public DistanceBoundGoal(Mob mob) {
		this(mob, DEFAULT_TICK_INTERVAL);
	}

	/**
	 * 严格从小到大设置边界值
	 * 
	 * @param boundDistances
	 */
	public DistanceBoundGoal setBoundDistances(double... boundDistances) {
		this.boundDistances = boundDistances;
		this.boundDistancesSqr = new double[boundDistances.length];
		for (int idx = 0; idx < boundDistances.length; ++idx) {
			boundDistancesSqr[idx] = boundDistances[idx] * boundDistances[idx];
		}
		return this;
	}

	public double[] getBoundDistances() {
		return boundDistances;
	}

	/**
	 * 获取边界索引
	 * 
	 * @param distanceSqr
	 * @return
	 */
	public int getDistanceSqrBoundLevel(double distanceSqr) {
		return Algorithms.getBoundLevel(distanceSqr, boundDistancesSqr);
	}

	@Override
	public boolean canUse() {
		if (this.checkMobTarget()) {
			this.updateDistance();
			return currentBoundLevel < boundDistancesSqr.length;// 更新当前currentBoundLevel值并检查
		} else {
			return false;
		}
	}

	protected void updateDistance() {
		currentDistanceSqr = this.distanceSqrToTarget();
		currentBoundLevel = this.getDistanceSqrBoundLevel(currentDistanceSqr);
	}

	public void update() {
		this.updateDistance();
		this.update(Math.sqrt(currentDistanceSqr), currentBoundLevel);
	}

	public void update(double currentDistance, int currentBoundLevel) {

	}
}
