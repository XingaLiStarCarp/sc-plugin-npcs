package minecraft.extended.gun.goal;

import minecraft.entity.goal.action.AttackGoal;
import minecraft.extended.gun.GunOperator.GeneralGunOperator;
import net.minecraft.world.entity.Mob;

public class GunAttackGoal extends AttackGoal {
	protected GeneralGunOperator gunOperator;

	public GunAttackGoal(Mob mob, int attackInterval) {
		super(mob, attackInterval);
		gunOperator = new GeneralGunOperator(mob);
		gunOperator.setReloadNeedCheckAmmo(false);// AI射击不需要检查是否有弹夹就可以直接换弹
		this.setBoundDistances(4, 32);
	}

	@Deprecated
	public GunAttackGoal(Mob mob) {
		this(mob, ATTRIBUTE_ATTACK_SPEED);
	}

	@Override
	public void attack(double currentDistance, int currentBoundLevel) {
		switch (currentBoundLevel) {
		case 0:
			gunOperator.aim(false);
			gunOperator.melee();
			break;
		case 1:
			gunOperator.aim(true);
			gunOperator.shootAuto(this.mob.getTarget());
			break;
		}
	}

	@Override
	public void exit() {
	}
}
