package minecraft.component.trait.entity;

import minecraft.entity.goal.MobGoalUtils;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

/**
 * 原版随机走动
 */
public class RandomWanderingTrait extends GoalTrait {
	/**
	 * 随机走动
	 * 
	 * @param panic              被攻击是否会恐慌乱跑
	 * @param panicSpeedModifier 恐慌跑动的速度增益
	 * @param walkPriority       随机走动优先级
	 * @param lookPriority       四处张望优先级
	 */
	protected RandomWanderingTrait(boolean panic, double panicSpeedModifier, int walkPriority, int lookPriority) {
		// 如果不是PathfinderMob则不添加随机走动的goal
		this.add(walkPriority, (mob) -> mob instanceof PathfinderMob pathfinderMob ? new WaterAvoidingRandomStrollGoal(pathfinderMob, 1.0) : null);
		this.add(lookPriority, (mob) -> new RandomLookAroundGoal(mob));
		if (panic) {
			this.add(walkPriority - 1, (mob) -> mob instanceof PathfinderMob pathfinderMob ? new PanicGoal(pathfinderMob, panicSpeedModifier) : null);// 比随机走动优先级高一级
		}
	}

	public RandomWanderingTrait(double panicSpeedModifier, int walkPriority, int lookPriority) {
		this(true, panicSpeedModifier, walkPriority, lookPriority);
	}

	public RandomWanderingTrait(int walkPriority, int lookPriority) {
		this(false, 1.0, walkPriority, lookPriority);
	}

	public RandomWanderingTrait(double panicSpeedModifier) {
		this(panicSpeedModifier, MobGoalUtils.PRIORITY_LOWEST, MobGoalUtils.PRIORITY_LOWEST);
	}

	public RandomWanderingTrait() {
		this(MobGoalUtils.PRIORITY_LOWEST, MobGoalUtils.PRIORITY_LOWEST);
	}
}
