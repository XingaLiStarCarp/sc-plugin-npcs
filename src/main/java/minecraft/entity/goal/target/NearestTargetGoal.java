package minecraft.entity.goal.target;

import java.util.function.BiPredicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * 瞄准实体的目标。<br>
 * 如果需要近战攻击瞄准的目标，则需要配合使用MeleeAttackGoal。<br>
 */
public class NearestTargetGoal extends GeneralTargetGoal {
	protected double searchHeight = 4.0;

	public NearestTargetGoal(Mob mob, boolean mustSee, boolean mustReach, boolean mustCanAttack, BiPredicate<Mob, LivingEntity> targetCondition) {
		super(mob, mustSee, mustReach, mustCanAttack, targetCondition);
	}

	public NearestTargetGoal(Mob mob, boolean mustSee, boolean mustReach, BiPredicate<Mob, LivingEntity> targetCondition) {
		super(mob, mustSee, mustReach, targetCondition);
	}

	public NearestTargetGoal(Mob mob, boolean mustSee, BiPredicate<Mob, LivingEntity> targetCondition) {
		super(mob, mustSee, targetCondition);
	}

	@Override
	protected LivingEntity findTarget(TargetingConditions targetConditions) {
		return this.findNearstTarget(this.getAABBSearchArea(searchHeight), targetConditions);
	}
}