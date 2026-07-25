package minecraft.component.trait.entity;

import minecraft.component.TraitProvider.TraitComponent;
import minecraft.registry.Registers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 手持物品
 * 
 * @param <_T>
 */
public class ItemHoldTrait implements TraitComponent<LivingEntity> {
	private LivingEntity entity;

	private ItemStack mainHandItem;
	private ItemStack offhandItem;

	private ItemStack originalMainHandItem;
	private ItemStack originalOffHandItem;

	/**
	 * 空物品的ID
	 */
	public static final String EMPTY_ID = Items.AIR.toString();
	public static final ItemStack EMPTY_ITEM = ItemStack.EMPTY;

	public ItemHoldTrait(ItemStack mainHandItem, ItemStack offhandItem) {
		this.mainHandItem = mainHandItem;
		this.offhandItem = offhandItem;
	}

	public ItemHoldTrait(ItemStack mainHandItem) {
		this(mainHandItem, EMPTY_ITEM);
	}

	public ItemHoldTrait(String mainHandItem, String offhandItem) {
		this(Registers.itemStack(mainHandItem), Registers.itemStack(offhandItem));
	}

	public ItemHoldTrait(String mainHandItem) {
		this(mainHandItem, EMPTY_ID);
	}

	public ItemStack getMainHandItem() {
		if (this.entity != null) {
			return this.entity.getItemInHand(InteractionHand.MAIN_HAND);
		}
		return null;
	}

	public ItemStack getOffHandItem() {
		if (this.entity != null) {
			return this.entity.getItemInHand(InteractionHand.OFF_HAND);
		}
		return null;
	}

	public void setMainHandItem(ItemStack mainHandItem) {
		this.mainHandItem = mainHandItem;
		if (this.entity != null) {
			this.entity.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
		}
	}

	public void setMainHandItem(String mainHandItem) {
		setMainHandItem(Registers.itemStack(mainHandItem));
	}

	public void setOffHandItem(ItemStack offhandItem) {
		this.offhandItem = offhandItem;
		if (this.entity != null) {
			this.entity.setItemInHand(InteractionHand.OFF_HAND, offhandItem);
		}
	}

	public void setOffHandItem(String offhandItem) {
		setMainHandItem(Registers.itemStack(offhandItem));
	}

	@Override
	public void init(LivingEntity entity) {
		this.entity = entity;
		this.originalMainHandItem = this.getMainHandItem();
		this.originalOffHandItem = this.getOffHandItem();
		this.setMainHandItem(mainHandItem);
		this.setOffHandItem(offhandItem);
	}

	@Override
	public void uninit(LivingEntity mob) {
		this.entity.setItemInHand(InteractionHand.MAIN_HAND, originalMainHandItem);
		this.entity.setItemInHand(InteractionHand.OFF_HAND, originalOffHandItem);
	}
}
