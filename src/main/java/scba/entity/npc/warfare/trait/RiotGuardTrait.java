package scba.entity.npc.warfare.trait;

import java.util.function.Supplier;

import minecraft.component.trait.MultiTrait;
import minecraft.component.trait.entity.GoalTrait;
import minecraft.component.trait.entity.ItemHoldTrait;
import minecraft.component.trait.entity.RandomWanderingTrait;
import minecraft.entity.goal.navigation.KeepDistanceToTargetGoal;
import minecraft.entity.goal.target.NearestTargetGoal;
import minecraft.extended.gun.GunOperator;
import minecraft.extended.gun.goal.GunAttackGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * 防暴警察，手枪配盾
 */
public class RiotGuardTrait extends MultiTrait {
	public static final Supplier<AttributeSupplier> ATTRIBUTES = () -> Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 20)
			.add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.FOLLOW_RANGE, 32)
			.add(Attributes.ARMOR, 20)
			.add(Attributes.ARMOR_TOUGHNESS, 8)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
			.add(Attributes.ATTACK_DAMAGE, 8)
			.add(Attributes.ATTACK_KNOCKBACK, 1)
			.add(Attributes.ATTACK_SPEED, 0.5)
			.build();

	// 带盾的手枪
	public static final String SHIELD_PISTOL = "ccrp:shield_ots33";

	public RiotGuardTrait(ItemStack gun) {
		super();
		this.add(new ItemHoldTrait(gun)); // 手持物品
		this.add(new RandomWanderingTrait());
		this.add(new GoalTrait()
				.add(0, (mob) -> new GunAttackGoal(mob, 50).setBoundDistances(2.5, 64))
				.add(2, (mob) -> new KeepDistanceToTargetGoal(mob, 0, 64.0))
				.add(3, (mob) -> new NearestTargetGoal(mob, true, true, (m, e) -> true)));
	}

	public RiotGuardTrait(String gun) {
		this(GunOperator.newGun(gun));
	}

	public RiotGuardTrait() {
		this(SHIELD_PISTOL);
	}
}
