package minecraft.component.trait.entity;

import minecraft.component.OpProvider;
import minecraft.component.trait.OpTrait;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 实体攻击/被攻击时的特性
 */
@EventBusSubscriber
public abstract class AttackTrait<_TargetEntity extends LivingEntity & OpProvider> extends OpTrait<_TargetEntity, LivingIncomingDamageEvent, AttackTrait<_TargetEntity>> {

	public AttackTrait() {
		super(LivingIncomingDamageEvent.class);
	}

	protected boolean operate(_TargetEntity target, LivingIncomingDamageEvent event) {
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
	public void onAttack(LivingIncomingDamageEvent event, LivingEntity damagee, LivingEntity target) {

	}

	/**
	 * 被攻击事件。<br>
	 * 
	 * @param event
	 * @param damager
	 * @param mob
	 */
	public void onAttacked(LivingIncomingDamageEvent event, Entity damager, LivingEntity target) {

	}

	/**
	 * 实体攻击/被攻击时执行的行为
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public static void onLivingIncomingDamageEvent(LivingIncomingDamageEvent event) {
		if (event.getSource().getEntity() instanceof OpProvider damager) {
			damager.executeOpComponent(LivingIncomingDamageEvent.class, event);
		}
		if (event.getEntity() instanceof OpProvider damagee) {
			damagee.executeOpComponent(LivingIncomingDamageEvent.class, event);
		}
	}
}
