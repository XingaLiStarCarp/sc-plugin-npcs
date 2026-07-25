package minecraft.entity.goal.target;

import java.util.EnumSet;

import minecraft.entity.goal.BaseGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

/**
 * 索敌Goal，从索敌成功到目标完成或丢失时一直保持运行中
 */
public abstract class TargetGoal extends BaseGoal {
	/**
	 * 当前的目标
	 */
	private LivingEntity target;

	/**
	 * 锁定目标
	 * 
	 * @param mob             索敌主体
	 * @param mustSee         是否只能索敌视线之内的
	 * @param mustReach       是否只能索敌有路径到达的目标
	 * @param targetCondition
	 */
	public TargetGoal(Mob mob) {
		super(mob, 1);// 每tick都需要更新
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	public double getFollowDistance() {
		return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
	}

	/**
	 * 判断目标是否在FOLLOW_RANGE内
	 * 
	 * @param target
	 * @return
	 */
	public boolean isInFollowDistance(LivingEntity target) {
		double followRange = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
		return this.mob.distanceToSqr(target) <= followRange * followRange;
	}

	@Override
	public boolean canUse() {
		if (target == null)
			return true;
		if (!target.isAlive())
			this.clearTarget();// 当前目标死亡时清除目标
		return target == null;
	}

	/**
	 * 寻找目标
	 * 
	 * @return
	 */
	protected abstract LivingEntity findTarget();

	@Override
	public void update() {
		LivingEntity currentTarget = this.mob.getTarget();
		// 当前目标为null时使用本Goal设置的目标
		if (currentTarget == null) {
			currentTarget = this.target;
		}
		if (currentTarget == null) {
			// 本Goal的目标也是null则执行索敌
			currentTarget = this.findTarget();
		}
		this.setTarget(currentTarget);
	}

	protected boolean canAttack(LivingEntity target, boolean checkTeam) {
		if (target == null)
			return false;
		boolean canAttack = this.mob.canAttack(target);
		return checkTeam ? canAttack && this.mob.getTeam() == target.getTeam() : canAttack;
	}

	/**
	 * 能否看见目标实体
	 * 
	 * @param target
	 * @return
	 */
	protected boolean canSee(LivingEntity target) {
		if (target == null)
			return false;
		else
			return this.mob.getSensing().hasLineOfSight(target);
	}

	/**
	 * 是否有路径可以到达目标
	 * 
	 * @param target           目标实体
	 * @param maxFinalDistance 路径终点距离目标的最大距离
	 * @param checkRestriction 额外检查是否在索敌主体可活动范围内
	 * @return
	 */
	protected boolean canReach(LivingEntity target, double maxFinalDistance, boolean checkRestriction) {
		if (target == null)
			return false;
		Path path = this.mob.getNavigation().createPath(target, 0);
		if (path == null) {
			return false;
		} else {
			// 获取路径终点
			Node node = path.getEndNode();
			if (node == null) {
				return false;
			} else {
				int dx = node.x - target.getBlockX();
				int dz = node.z - target.getBlockZ();
				// 判断目标终点与实体的距离
				boolean inDistance = (dx * dx + dz * dz) <= maxFinalDistance;
				return checkRestriction ? inDistance && this.mob.isWithinRestriction(target.blockPosition()) : inDistance;
			}
		}
	}

	protected AABB getAABBSearchArea(double rangeX, double height, double rangeZ) {
		return this.mob.getBoundingBox().inflate(rangeX, height, rangeZ);
	}

	protected AABB getAABBSearchArea(double range, double height) {
		return this.getAABBSearchArea(range, height, range);
	}

	/**
	 * 依据Attributes.FOLLOW_RANGE属性获取搜索范围
	 * 
	 * @param height 搜索垂直高度
	 * @return
	 */
	protected AABB getAABBSearchArea(double height) {
		return this.getAABBSearchArea(this.getFollowDistance(), height);
	}

	protected LivingEntity findNearstTarget(AABB searchArea, TargetingConditions targetConditions) {
		return this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(LivingEntity.class, searchArea, (entity) -> {
			return entity != this.mob;
		}), targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
		this.mob.setTarget(target);
	}

	public void clearTarget() {
		this.setTarget(null);
	}
}
