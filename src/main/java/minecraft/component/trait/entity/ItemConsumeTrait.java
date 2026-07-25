package minecraft.component.trait.entity;

import minecraft.ItemsList;
import minecraft.component.OpProvider;
import minecraft.entity.EntityInteractions;
import net.minecraft.world.entity.LivingEntity;

/**
 * 从玩家主手扣除物品的特性
 * 
 * @param <_TargetEntity>
 */
public abstract class ItemConsumeTrait<_TargetEntity extends LivingEntity & OpProvider> extends CombinedInteractionTrait<_TargetEntity> {

	@SuppressWarnings("unchecked")
	public ItemConsumeTrait<_TargetEntity> setReceiveItems(ItemsList items, boolean hold) {
		int num = items.size();
		CombinedInteractionOp<_TargetEntity>[] ops = new CombinedInteractionOp[num];
		for (int idx = 0; idx < num; ++idx) {
			final int i = idx;
			ops[idx] = (event, player, entity, hand) -> {
				if (hold)
					return EntityInteractions.receiveItemFromPlayerMainHandAndHold(player, entity, items.getItem(i));
				else
					return EntityInteractions.receiveItemFromPlayerMainHand(player, items.getItem(i));
			};
		}
		this.setOp(ops);
		return this;
	}
}
