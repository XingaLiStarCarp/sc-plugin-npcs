package minecraft.extended.gun.goal;

import minecraft.entity.goal.action.AttackGoal;
import minecraft.extended.gun.GunOperator.GeneralGunOperator;
import net.minecraft.world.entity.Mob;

public class GunAttackGoal extends AttackGoal {
	protected GeneralGunOperator gunOperator;
	protected double spread;

	public GunAttackGoal(Mob mob, int attackInterval, double spread) {
		super(mob, attackInterval);
		gunOperator = new GeneralGunOperator(mob);
		gunOperator.setReloadNeedCheckAmmo(false);// AI射击不需要检查是否有弹夹就可以直接换弹
		this.spread = spread;
		this.setBoundDistances(4, 32);
	}

	@Deprecated
	public GunAttackGoal(Mob mob, double spread) {
		this(mob, ATTRIBUTE_ATTACK_SPEED, spread);
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
			// 射击实体，而不是坐标。有的武器可能就是锁实体头的
			gunOperator.shootAuto(this.mob.getTarget().getEyePosition(), spread);
			break;
		}
	}

	@Override
	public void exit() {
	}
}
