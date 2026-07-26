package scba.entity.npc.warfare.trait;

import java.util.function.Supplier;

import com.tacz.guns.api.item.GunTabType;

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
import scba.util.GunUtils;

/**
 * 狙击手，远距离狙击
 */
public class SniperTrait extends MultiTrait {
	public static final Supplier<AttributeSupplier> ATTRIBUTES = () -> Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 20)
			.add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.FOLLOW_RANGE, 64)
			.add(Attributes.ARMOR, 12)
			.add(Attributes.ARMOR_TOUGHNESS, 8)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0)
			.add(Attributes.ATTACK_DAMAGE, 16)
			.add(Attributes.ATTACK_KNOCKBACK, 3)
			.add(Attributes.ATTACK_SPEED, 1)
			.build();

	public SniperTrait(ItemStack gun) {
		super();
		this.add(new ItemHoldTrait(gun)); // 手持物品
		this.add(new RandomWanderingTrait());
		this.add(new GoalTrait()
				.add(0, (mob) -> new GunAttackGoal(mob, 50, 0.4).setBoundDistances(2.5, 64))
				.add(2, (mob) -> new KeepDistanceToTargetGoal(mob, 0, 64.0))
				.add(3, (mob) -> new NearestTargetGoal(mob, true, true, (m, e) -> true)));
	}

	public SniperTrait(String gun) {
		this(GunOperator.newGun(gun));
	}

	public SniperTrait() {
		this(GunUtils.getRandomGunWithAttachments(GunTabType.SNIPER));
	}
}
