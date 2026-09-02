package minecraft.component.trait.entity;

import minecraft.component.OpProvider;
import minecraft.component.trait.DualEndedTrait;
import minecraft.component.trait.OpTrait;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 交互特性类。<br>
 * 交互事件是双端事件，客户端与服务端会分别触发一次，如果是本地游戏则触发两次。<br>
 */
@EventBusSubscriber
public abstract class InteractionTrait<_TargetEntity extends Entity & OpProvider> extends OpTrait<_TargetEntity, PlayerInteractEvent.EntityInteract, InteractionTrait<_TargetEntity>> implements DualEndedTrait<_TargetEntity> {
	private final boolean isClient;

	@Override
	public final boolean isClient() {
		return isClient;
	}

	public InteractionTrait(boolean isClient) {
		super(PlayerInteractEvent.EntityInteract.class);
		this.isClient = isClient;
	}

	/**
	 * 服务端执行
	 */
	public InteractionTrait() {
		this(false);
	}

	@Override
	protected boolean operate(_TargetEntity target, PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		// 判断端
		if (this.validate(player)) {
			InteractionHand hand = event.getHand();
			if (player.getUsedItemHand() == hand) {
				this.interact(event, player, target, hand);
			}
		}
		return true;
	}

	/**
	 * 交互事件。<br>
	 * 
	 * @param player
	 * @param mob
	 * @param hand
	 * @param isClient
	 */
	public abstract void interact(PlayerInteractEvent.EntityInteract event, Player player, _TargetEntity target, InteractionHand hand);

	/**
	 * 实体被交互时执行的行为
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.getTarget() instanceof OpProvider opProvider) {
			opProvider.executeOpComponent(PlayerInteractEvent.EntityInteract.class, event);
		}
	}
}
