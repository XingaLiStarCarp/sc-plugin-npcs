package scba.entity.npc.warfare.trait;

import java.util.function.Supplier;

import minecraft.component.trait.MultiTrait;
import minecraft.component.trait.entity.GoalTrait;
import minecraft.component.trait.entity.ItemHoldTrait;
import minecraft.component.trait.entity.RandomWanderingTrait;
import minecraft.component.trait.entity.StepParticlesTrait;
import minecraft.entity.goal.action.MeleeAttackGoal;
import minecraft.entity.goal.action.UseItemGoal;
import minecraft.entity.goal.navigation.SprintKeepDistanceToTargetGoal;
import minecraft.entity.goal.target.NearestTargetGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 典狱长，冲刺近战攻击
 */
public class WardenTrait extends MultiTrait {
	public static final Supplier<AttributeSupplier> ATTRIBUTES = () -> Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 20)
			.add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.FOLLOW_RANGE, 32)
			.add(Attributes.ARMOR, 20)
			.add(Attributes.ARMOR_TOUGHNESS, 8)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
			.add(Attributes.ATTACK_DAMAGE, 8)
			.add(Attributes.ATTACK_KNOCKBACK, 1)
			.add(Attributes.ATTACK_SPEED, 2)
			.build();

	public static final String MAIN_HAND_ITEM = "minecraft:iron_sword";
	public static final String OFFHAND_HAND_ITEM = "minecraft:shield";

	public WardenTrait(String mainHandItem, String offhandItem, int speedUpTicks) {
		super();
		this.add(new ItemHoldTrait(mainHandItem, offhandItem)); // 手持物品
		this.add(new RandomWanderingTrait());
		this.add(new StepParticlesTrait(2.0));// 地面跑动有方块粒子特效
		this.add(new GoalTrait()
				.add(0, (mob) -> new MeleeAttackGoal(mob, 2, 15).setBoundDistances(2.5))
				.add(1, (mob) -> new UseItemGoal(mob, true).setBoundDistances(2))
				.add(2, (mob) -> new SprintKeepDistanceToTargetGoal(mob, 0, 1.5, 3.0, speedUpTicks))
				.add(3, (mob) -> new NearestTargetGoal(mob, true, true, (m, e) -> true)));
	}

	public WardenTrait(int speedUpTicks) {
		this(MAIN_HAND_ITEM, OFFHAND_HAND_ITEM, speedUpTicks);
	}
}