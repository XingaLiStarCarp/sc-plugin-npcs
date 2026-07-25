package minecraft.component.trait.entity;

import minecraft.component.OpProvider;
import minecraft.component.trait.OpTrait;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * 实体攻击/被攻击时的特性
 */
@EventBusSubscriber(bus = Bus.FORGE)
public abstract class AttackTrait<_TargetEntity extends LivingEntity & OpProvider> extends OpTrait<_TargetEntity, LivingAttackEvent, AttackTrait<_TargetEntity>> {

	public AttackTrait() {
		super(LivingAttackEvent.class);
	}

	protected boolean operate(_TargetEntity target, LivingAttackEvent event) {
		LivingEntity damagee = event.getEntity();
		if (target == damagee) {
			this.onAttacked(event, event.getSource().getEntity(), target);
		} else {
			this.onAttack(event, damagee, target);
		}
		return true;
	}

	/**
	 * 主动攻击事件。<br>
	 * 
	 * @param event
	 * @param damagee
	 * @param mob
	 */
	public void onAttack(LivingAttackEvent event, LivingEntity damagee, LivingEntity target) {

	}

	/**
	 * 被攻击事件。<br>
	 * 
	 * @param event
	 * @param damager
	 * @param mob
	 */
	public void onAttacked(LivingAttackEvent event, Entity damager, LivingEntity target) {

	}

	/**
	 * 实体攻击/被攻击时执行的行为
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event) {
		if (event.getSource().getEntity() instanceof OpProvider damager) {
			damager.executeOpComponent(LivingAttackEvent.class, event);
		}
		if (event.getEntity() instanceof OpProvider damagee) {
			damagee.executeOpComponent(LivingAttackEvent.class, event);
		}
	}
}
