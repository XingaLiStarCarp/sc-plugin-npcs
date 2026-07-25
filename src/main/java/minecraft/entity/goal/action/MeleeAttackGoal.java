package minecraft.entity.goal.action;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;

public class MeleeAttackGoal extends AttackGoal {
	private double attackRange;

	public MeleeAttackGoal(Mob mob, double attackRange, int attackInterval) {
		super(mob, attackInterval);
		this.attackRange = attackRange;
		this.faceToleranceAngle = 120.0f;
	}

	@Deprecated
	public MeleeAttackGoal(Mob mob, double attackRange) {
		this(mob, attackRange, ATTRIBUTE_ATTACK_SPEED);
	}

	public void attack(double currentDistance, int currentBoundLevel) {
		if (currentDistance <= attackRange) {
			this.mob.swing(InteractionHand.MAIN_HAND);
			this.mob.doHurtTarget(mob.getTarget());
		}
	}
}
