package minecraft.component.trait.entity;

import java.util.function.Function;

import minecraft.component.OpProvider;
import minecraft.entity.EntityInteractions.CombinedTask;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent; 

/**
 * 组合动作。<br>
 * 客户端和服务端需要分开定义为两个不同的类，防止服务端运行时链接客户端类。<br>
 */
public abstract class CombinedInteractionTrait<_TargetEntity extends Entity & OpProvider> extends InteractionTrait<_TargetEntity> {

	private CombinedTask consumeItemsTask;

	/**
	 * 所有交互都成功的动作
	 */
	public abstract void onSuccess(PlayerInteractEvent.EntityInteract event, Player player, _TargetEntity target, InteractionHand hand);

	/**
	 * 单次交互成功的动作
	 */
	public void onSingleSuccess(PlayerInteractEvent.EntityInteract event, Player player, _TargetEntity target, InteractionHand hand) {

	}

	/**
	 * 没有全部交互成功的动作
	 */
	public void onAnyFailed(PlayerInteractEvent.EntityInteract event, Player player, _TargetEntity target, InteractionHand hand) {

	}

	@FunctionalInterface
	public static interface CombinedInteractionOp<_TargetEntity extends Entity & OpProvider> {
		public abstract boolean interact(PlayerInteractEvent.EntityInteract event, Player player, _TargetEntity target, InteractionHand hand);
	}

	/**
	 * 将交互操作函数转换为CombinedTask使用的函数
	 * 
	 * @param op
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <_TargetEntity extends Entity & OpProvider> Function<Object[], Boolean> combineTaskInteraction(final CombinedInteractionOp<_TargetEntity> op) {
		return (Object... args) -> {
			return op.interact((PlayerInteractEvent.EntityInteract) args[0], (Player) args[1], (_TargetEntity) args[2], (InteractionHand) args[3]);
		};
	}

	@SuppressWarnings("unchecked")
	public static final <_TargetEntity extends Entity & OpProvider> Function<Object[], Boolean>[] combineTaskInteractions(CombinedInteractionOp<_TargetEntity>... ops) {
		Function<Object[], Boolean>[] convertedOps = new Function[ops.length];
		for (int i = 0; i < ops.length; ++i) {
			convertedOps[i] = combineTaskInteraction(ops[i]);
		}
		return convertedOps;
	}

	public CombinedInteractionTrait(boolean isClient) {
		super(isClient);
	}

	public CombinedInteractionTrait() {
		this(false);
	}

	@SuppressWarnings("unchecked")
	protected CombinedInteractionTrait<_TargetEntity> setOp(CombinedInteractionOp<_TargetEntity>... ops) {
		this.consumeItemsTask = new CombinedTask(
				(Object... args) -> this.onSingleSuccess((PlayerInteractEvent.EntityInteract) args[0], (Player) args[1], (_TargetEntity) args[2], (InteractionHand) args[3]),
				(Object... args) -> this.onAnyFailed((PlayerInteractEvent.EntityInteract) args[0], (Player) args[1], (_TargetEntity) args[2], (InteractionHand) args[3]),
				(Object... args) -> this.onSuccess((PlayerInteractEvent.EntityInteract) args[0], (Player) args[1], (_TargetEntity) args[2], (InteractionHand) args[3]),
				combineTaskInteractions(ops));
		return this;
	}

	@Override
	public void interact(PlayerInteractEvent.EntityInteract event, Player player, _TargetEntity target, InteractionHand hand) {
		consumeItemsTask.execute(event, player, target, hand);
	}
}
