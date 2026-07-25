package minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 物品清单
 */
public class ItemsList {
	private final ArrayList<ItemStack> list;

	private ItemsList(ItemStack... items) {
		list = new ArrayList<>(items.length);
		list.addAll(List.of(items));
	}

	public static final ItemsList of(ItemStack... items) {
		return new ItemsList(items);
	}

	public static final ItemStack item(Item type, int count) {
		return new ItemStack(type, count);
	}

	public int size() {
		return list.size();
	}

	/**
	 * 为物品清单新增
	 * 
	 * @param newItem
	 * @return
	 */
	public ItemsList add(ItemStack newItem) {
		Item newType = newItem.getItem();
		int newCount = newItem.getCount();
		final int maxStackSize = newType.getMaxStackSize(newItem);
		for (ItemStack curretnItem : list) {
			Item currentType = curretnItem.getItem();
			if (currentType == newType) {// 已经存在同种物品则堆叠到已有物品上
				if (curretnItem.getDamageValue() == newItem.getDamageValue()) {// 必须metadata全部一样才能堆叠
					int finalCount = curretnItem.getCount() + newCount;
					if (finalCount > maxStackSize) {
						curretnItem.setCount(maxStackSize);
						newCount -= (finalCount - maxStackSize);
					} else {
						curretnItem.setCount(finalCount);
						return this;
					}
				}
			}
		}
		if (newCount > 0) {// 遍历完了原本全部的物品仍然不够放置，需要新增ItemStack
			newItem.setCount(newCount);
			list.add(newItem);
		}
		return this;
	}

	/**
	 * 从背包中移除所有该清单的物品。<br>
	 * 如果背包具有足够的物品，则从背包中移除并返回true。<br>
	 * 如果背包内的物品不足，则不会移除任何物品并返回false。<br>
	 * 
	 * @param inv
	 * @return
	 */
	public boolean removeIfExists(Inventory inv) {
		return false;
	}

	/**
	 * 将物品导出到背包
	 * 
	 * @param inv
	 */
	public void export(Inventory inv) {
		for (ItemStack curretnItem : list) {
			inv.add(curretnItem);
		}
	}

	public ItemStack getItem(int idx) {
		return list.get(idx);
	}

	public int totalCount() {
		int total = 0;
		for (ItemStack curretnItem : list) {
			total += curretnItem.getCount();
		}
		return total;
	}

	public boolean isEmpty() {
		return totalCount() == 0;
	}
}
