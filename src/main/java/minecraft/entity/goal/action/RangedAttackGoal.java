package minecraft.entity.goal.action;

import net.minecraft.world.entity.Mob;

/**
 * 远程攻击目标
 */
public class RangedAttackGoal extends AttackGoal {
	public RangedAttackGoal(Mob mob, int attackInterval) {
		super(mob, attackInterval);
	}

	public RangedAttackGoal(Mob mob) {
		super(mob);
	}

	@Override
	public void attack(double currentDistance, int currentBoundLevel) {
	}
}
