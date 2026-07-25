package minecraft.entity.goal.target;

import java.util.function.BiPredicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class GeneralTargetGoal extends TargetGoal {
	protected boolean mustSee;
	protected boolean mustReach;
	protected boolean mustCanAttack;
	protected final double searchDistance;
	private final TargetingConditions targetConditions;

	protected double reachMaxFinalDistance = 2.25;
	protected boolean reachCheckRestriction = true;
	protected boolean attackCheckTeam = true;

	public static final double SEARCH_DISTABCE_ATTRIBUTE_FOLLOW_RANGE = Double.NaN;

	public GeneralTargetGoal(Mob mob, boolean forCombat, double searchDistance, boolean mustSee, boolean mustReach, boolean mustCanAttack, BiPredicate<Mob, LivingEntity> targetCondition) {
		super(mob);
		this.searchDistance = searchDistance;
		this.mustSee = mustSee;
		this.mustReach = mustReach;
		this.mustCanAttack = mustCanAttack;
		this.targetConditions = (forCombat ? TargetingConditions.forCombat() : TargetingConditions.forNonCombat())
				.range(Double.isNaN(searchDistance) ? this.getFollowDistance() : searchDistance)
				.selector((entity) -> {
					if (!entity.isAlive())// 过滤死亡实体
						return false;
					boolean canSee = this.mustSee ? this.canSee(mob) : true;
					boolean canReach = this.mustReach ? this.canReach(mob, this.reachMaxFinalDistance, this.reachCheckRestriction) : true;
					boolean canAttack = this.mustCanAttack ? this.canAttack(mob, this.attackCheckTeam) : true;
					return canSee && canReach && canAttack && targetCondition.test(this.mob, entity);
				});
	}

	public GeneralTargetGoal(Mob mob, boolean forCombat, boolean mustSee, boolean mustReach, boolean mustCanAttack, BiPredicate<Mob, LivingEntity> targetCondition) {
		this(mob, forCombat, SEARCH_DISTABCE_ATTRIBUTE_FOLLOW_RANGE, mustSee, mustReach, mustCanAttack, targetCondition);
	}

	public GeneralTargetGoal(Mob mob, boolean mustSee, boolean mustReach, boolean mustCanAttack, BiPredicate<Mob, LivingEntity> targetCondition) {
		this(mob, true, mustSee, mustReach, mustCanAttack, targetCondition);
	}

	public GeneralTargetGoal(Mob mob, boolean mustSee, boolean mustReach, BiPredicate<Mob, LivingEntity> targetCondition) {
		this(mob, mustSee, mustReach, true, targetCondition);
	}

	public GeneralTargetGoal(Mob mob, boolean mustSee, BiPredicate<Mob, LivingEntity> targetCondition) {
		this(mob, mustSee, false, targetCondition);
	}

	protected abstract LivingEntity findTarget(TargetingConditions targetConditions);

	@Override
	protected LivingEntity findTarget() {
		return this.findTarget(targetConditions);
	}
}
