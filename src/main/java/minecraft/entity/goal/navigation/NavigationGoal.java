package minecraft.entity.goal.navigation;

import java.util.EnumSet;

import minecraft.entity.data.EntityData;
import minecraft.entity.goal.BaseGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

/**
 * 导航通用Goal
 */
public abstract class NavigationGoal extends BaseGoal {
	/**
	 * 是否可以打断其他类型为MOVE的Goal
	 */
	protected boolean interrupt;

	/**
	 * 每多少tick执行一次路径规划
	 */
	protected int updateTicks;

	protected final PathNavigation navigation;

	/**
	 * 当前的目标点
	 */
	protected Vec3 targetPos;

	/**
	 * targetPos是否有更新，只有此值被标志为true才会导致targetPosTotalTicks计数重置。
	 * 如果仅仅是targetPos引用改变，将不影响。
	 */
	protected boolean targetDirty;

	/**
	 * 从更改targetPos目标引用直到现在的总tick次数。<br>
	 */
	private int targetPosTotalTicks = 0;

	/**
	 * 默认的每次更新路径规划的时间间隔
	 */
	public static final int DEFAULT_UPDATE_TICKS = 5;

	public NavigationGoal(Mob mob, boolean interrupt, int updateTicks) {
		super(mob);
		this.navigation = mob.getNavigation();
		this.interrupt = interrupt;
		this.updateTicks = updateTicks;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	protected void updateTargetPos() {

	}

	@Override
	public boolean canUse() {
		this.updateTargetPos();// canUse()判断通过之前update()的内容都不会执行，因此判断之前先更新目标
		return this.mob.isAlive()
				&& this.checkTarget()
				&& (this.interrupt || !this.navigation.isInProgress());// 可以打断其他MOVE类型的Goal，或当前navigation在空闲状态
	}

	@Override
	public boolean canContinueToUse() {
		this.updateTargetPos();
		return this.checkTarget() && !isNavigationComplete();
	}

	public boolean checkTarget() {
		return this.targetPos != null;
	}

	/**
	 * 判断目标点引用是否有更新
	 */
	protected void checkTargetUpdate() {
		if (targetDirty) {
			targetPosTotalTicks = 0;
			targetDirty = false;
		} else if (targetPos != null) {
			++targetPosTotalTicks;// 如果是同一个目标位置引用，则更新计数
		}
	}

	public final int getTargetTicks() {
		return targetPosTotalTicks;
	}

	@Override
	public void update() {
		this.checkTargetUpdate();
		// 执行路径规划
		if (this.checkTarget()) {
			if (this.mob.tickCount % updateTicks == 0) {
				if (this.isNavigationComplete()) {
					this.navigation.stop();// 满足到达终点的条件，结束导航
				} else {
					Vec3 dx = this.mob.position().subtract(targetPos);
					updateMovement(targetPos, dx.normalize(), dx.length());
				}
			}
		} else {
			this.navigation.stop(); // 目标丢失则停止当前Goal
		}
	}

	@Override
	public void exit() {
		this.navigation.stop(); // 停止导航，实体静止
	}

	public final double getCurrentSpeedModifier() {
		return EntityData.getSpeedModifier(navigation);
	}

	public final void setCurrentSpeedModifier(double speedModifier) {
		navigation.setSpeedModifier(speedModifier);
	}

	/**
	 * 获取mob的目标的位置
	 * 
	 * @param entity
	 * @return
	 */
	public final Vec3 retrieveTargetPos(Mob entity) {
		if (entity == null)
			return null;
		LivingEntity target = entity.getTarget();
		if (target == null)
			return null;
		return target.position();
	}

	public final Vec3 retrieveTargetPos() {
		return retrieveTargetPos(this.mob);
	}

	/**
	 * 精准移动到指定点。<br>
	 * createPath()最后的int参数为目标位置误差判定，默认为1，会导致在运动距离小于1时实体停止不动认为已经完成了寻路。<br>
	 * 因此，需要手动创建一个误差为0的Path防止实体因为移动距离太短卡在原地不动。<br>
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param speedModifier
	 * @return
	 */
	public final boolean moveTo(double x, double y, double z, double speedModifier) {
		return this.navigation.moveTo(this.navigation.createPath(x, y, z, 0), speedModifier);
	}

	protected abstract boolean isNavigationComplete();

	/**
	 * 更新目标点。<br>
	 * 
	 * @param targetPos 目标点坐标
	 * @param direction 当前mob朝向目标点的方向
	 * @param distance  当前距离目标点的直线距离
	 */
	protected abstract void updateMovement(Vec3 targetPos, Vec3 direction, double distance);
}
