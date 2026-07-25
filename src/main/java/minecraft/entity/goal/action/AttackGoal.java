package minecraft.entity.goal.action;

import java.util.EnumSet;

import minecraft.TimeUtils;
import minecraft.entity.EntityInteractions;
import minecraft.entity.goal.DistanceBoundGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class AttackGoal extends DistanceBoundGoal {
	protected int attackInterval;
	/**
	 * 攻击时实体是否面朝目标
	 */
	protected boolean mustFace = true;
	protected float faceToleranceAngle = 60.0f;
	/**
	 * 左右旋转速度
	 */
	protected float yMaxRotSpeed = 30.0f;
	/**
	 * 俯仰角上下最大偏离角度
	 */
	protected float xMaxRotAngle = 30.0f;

	/**
	 * 使用实体的ATTACK_SPEED属性计算攻击间隔<br>
	 * 尽管设置了该值，但运行时始终为0，请勿使用。<br>
	 */
	@Deprecated
	public static final int ATTRIBUTE_ATTACK_SPEED = -1;

	public AttackGoal(Mob mob, int attackInterval) {
		super(mob);
		this.attackInterval = attackInterval;
		this.setFlags(EnumSet.of(Goal.Flag.LOOK));
	}

	@Deprecated
	public AttackGoal(Mob mob) {
		this(mob, ATTRIBUTE_ATTACK_SPEED);
	}

	protected double getAttackSpeed() {
		return this.mob.getAttributeValue(Attributes.ATTACK_SPEED);
	}

	@Override
	public boolean canUse() {
		return this.checkMobTarget();
	}

	@Override
	public void update(double currentDistance, int currentBoundLevel) {
		LivingEntity target = mob.getTarget();
		this.lookAt(target, yMaxRotSpeed, xMaxRotAngle);// 攻击时朝向目标
		// 判断是否面对目标
		if (mustFace ? EntityInteractions.isEntityFacing(this.mob, target.getEyePosition(), faceToleranceAngle) : true) {
			if (this.attackInterval > 0) {
				// 手动设置了attackInterval
				this.attack(currentDistance, currentBoundLevel);
			} else {
				// 使用实体的的attack speed
				int attributeAttackInterval = (int) (TimeUtils.TICKS_PER_SECOND / this.getAttackSpeed());
				if (this.getTicks() % attributeAttackInterval == 0) {
					this.attack(currentDistance, currentBoundLevel);
				}
			}
		}
	}

	public abstract void attack(double currentDistance, int currentBoundLevel);
}
