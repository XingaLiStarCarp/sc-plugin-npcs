package minecraft.entity.goal;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MobGoalUtils {
	public static final int PRIORITY_LOWEST = Integer.MAX_VALUE;
	public static final int PRIORITY_HIGHEST = Integer.MIN_VALUE;

	/**
	 * 当前位置的脚下方块是否是可站立的。
	 * 
	 * @param level
	 * @param pos
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static final boolean isStandable(Level level, Vec3 pos) {
		BlockPos groundPos = new BlockPos((int) pos.x, (int) pos.y - 1, (int) pos.z);
		if (!level.isInWorldBounds(groundPos)) {
			return false;
		} else {
			return level.getBlockState(groundPos).isSolid();
		}
	}

	public static Predicate<LivingEntity> entityHolds(Item item) {
		return (entity) -> entity.getMainHandItem().is(item);
	}

	public static Predicate<LivingEntity> entityCategory(MobCategory category) {
		return (entity) -> entity.getType().getCategory() == category;
	}
}
