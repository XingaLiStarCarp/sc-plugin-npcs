package minecraft.entity.goal.navigation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 让实体与瞄准的target实体保持一定距离的Goal
 */
public class KeepDistanceToTargetGoal extends KeepDistanceGoal {
	private LivingEntity prevTarget;

	public KeepDistanceToTargetGoal(Mob mob, double keepDistanceMin, double keepDistanceMax, double speedModifier, boolean interrupt, int updateTicks) {
		super(mob, keepDistanceMin, keepDistanceMax, speedModifier, interrupt, updateTicks);
	}

	public KeepDistanceToTargetGoal(Mob mob, double keepDistanceMin, double keepDistanceMax) {
		super(mob, keepDistanceMin, keepDistanceMax);
	}

	@Override
	protected void updateTargetPos() {
		this.targetPos = this.retrieveTargetPos();// 实时更新目标实体的位置
		LivingEntity currentTarget = this.mob.getTarget();
		this.targetDirty = (prevTarget != currentTarget);// 只要是同一个目标实体，即使坐标变化也不标记目标改变
		this.prevTarget = currentTarget;
	}
}
