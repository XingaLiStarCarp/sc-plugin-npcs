package minecraft.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import minecraft.TimeUtils;
import minecraft.registry.Registers;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 交互相关动作。
 */
public class EntityInteractions {
	public static class CombinedTask {
		Consumer<Object[]> onSingleSuccess;
		Consumer<Object[]> onAnyFailed;
		Consumer<Object[]> onSuccess;
		Function<Object[], Boolean>[] conds;
		boolean[] successState;// 记录多个操作的独立执行结果

		/**
		 * 组合不定时的执行的多个操作，每次操作都会记录结果，当所有操作全部执行完毕时才会返回true。<br>
		 * 执行结果已经为true的操作，不会再次执行。<br>
		 * 当全部操作执行都返回true时，整个组合操作完成并返回true，同时重置各个独立操作的完成状态。
		 * 
		 * @param onSingleSuccess 单次交互成功的动作
		 * @param onAnyFailed     没有全部交互成功的动作
		 * @param onSuccess       所有交互都成功的动作
		 * @param conds
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public CombinedTask(Consumer<Object[]> onSingleSuccess, Consumer<Object[]> onAnyFailed, Consumer<Object[]> onSuccess, Function<Object[], Boolean>... conds) {
			this.onSingleSuccess = onSingleSuccess;
			this.onAnyFailed = onAnyFailed;
			this.onSuccess = onSuccess;
			this.conds = conds;
			this.successState = new boolean[conds.length];
		}

		public boolean execute(Object... args) {
			boolean thisPassSuccess = true;// 本次是否所有操作都执行成功
			for (int i = 0; i < successState.length; ++i) {
				if (!successState[i]) {
					if (conds[i].apply(args)) {
						successState[i] = true;
						if (onSingleSuccess != null)
							onSingleSuccess.accept(args);
					} else
						thisPassSuccess = false;
				}
			}
			if (thisPassSuccess) {
				// 列表里的全部操作执行完成
				Arrays.fill(successState, false);// 重置各个操作的执行结果
				if (onSuccess != null)
					onSuccess.accept(args);
				return true;
			} else {
				if (onAnyFailed != null)
					onAnyFailed.accept(args);
				return false;
			}
		}
	}

	public static boolean receiveItemFromPlayerMainHand(Player player, Item type, int count) {
		ItemStack items = player.getMainHandItem();
		if (items.getItem() == type) {
			int currentCount = items.getCount();
			if (currentCount < count) {
				return false;
			} else {
				items.setCount(currentCount - count);
				return true;
			}
		} else {
			return false;
		}
	}

	public static boolean receiveItemFromPlayerMainHand(Player player, String type, int count) {
		return receiveItemFromPlayerMainHand(player, Registers.item(type), count);
	}

	public static boolean receiveItemFromPlayerMainHand(Player player, ItemStack recvItem) {
		return receiveItemFromPlayerMainHand(player, recvItem.getItem(), recvItem.getCount());
	}

	/**
	 * 查找玩家背包的物品
	 * 
	 * @param inv
	 * @param item
	 * @return
	 */
	public static final ArrayList<ItemStack> filterInventory(Inventory inv, ItemStack item) {
		ArrayList<ItemStack> result = new ArrayList<>();
		for (ItemStack current : inv.items) {
			if (current.getItem() == item.getItem() && current.getDamageValue() == item.getDamageValue()) {
				result.add(current);
			}
		}
		return result;
	}

	public static final ArrayList<ItemStack> filterInventory(Inventory inv, Item item) {
		ArrayList<ItemStack> result = new ArrayList<>();
		for (ItemStack current : inv.items) {
			if (current.getItem() == item) {
				result.add(current);
			}
		}
		return result;
	}

	public static boolean receiveItemFromPlayerInventory(Player player, Item type, int count) {
		if (count <= 0)
			return true;
		int requiredCount = count;
		Collection<? extends ItemStack> itemstacks = filterInventory(player.getInventory(), type);
		ArrayList<ItemStack> toBeCleared = new ArrayList<>();
		for (ItemStack itemstack : itemstacks) {
			if (requiredCount > 0) {
				int currentCount = itemstack.getCount();
				if (currentCount < requiredCount) {
					toBeCleared.add(itemstack);
					requiredCount -= currentCount;
				} else {
					itemstack.setCount(currentCount - requiredCount);
					for (ItemStack c : toBeCleared) {
						c.setCount(0);
					}
					return true;
				}
			}
		}
		return false;// 全部遍历完成，数量还是不够则返回false
	}

	public static boolean receiveItemFromPlayerInventory(Player player, String type, int count) {
		return receiveItemFromPlayerInventory(player, Registers.item(type), count);
	}

	public static void giveItemToPlayer(Player player, ItemStack give_items) {
		player.getInventory().add(give_items);
	}

	public static void sengMsgToPlayer(Player player, String msg) {
		player.sendSystemMessage(Component.literal(msg));
	}

	public static void holdItem(LivingEntity entity, ItemStack item) {
		entity.setItemInHand(InteractionHand.MAIN_HAND, item);
	}

	public static void clearHoldItem(LivingEntity entity, ItemStack item) {
		holdItem(entity, ItemStack.EMPTY);
	}

	/**
	 * 从玩家主手获取物品，并手持获取到的物品
	 * 
	 * @param type
	 * @param count
	 * @return
	 * @return
	 */
	public static boolean receiveItemFromPlayerMainHandAndHold(Player player, LivingEntity entity, Item type, int count) {
		ItemStack items = player.getMainHandItem();
		if (items.getItem() == type) {
			int currentCount = items.getCount();
			if (currentCount < count) {
				return false;
			} else {
				items.setCount(currentCount - count);
				ItemStack hold = items.copy();// 手持物品，需要拷贝
				hold.setCount(count);
				holdItem(entity, hold);
				return true;
			}
		} else {
			return false;
		}
	}

	public static boolean receiveItemFromPlayerMainHandAndHold(Player player, LivingEntity entity, String type, int count) {
		return receiveItemFromPlayerMainHandAndHold(player, entity, Registers.item(type), count);
	}

	public static boolean receiveItemFromPlayerMainHandAndHold(Player player, LivingEntity entity, ItemStack recvItem) {
		return receiveItemFromPlayerMainHandAndHold(player, entity, recvItem.getItem(), recvItem.getCount());
	}

	public static final void pause(Entity entity) {
		entity.setDeltaMovement(Vec3.ZERO);// 停止移动
		entity.setSilent(true);// 停止实体声音
	}

	/**
	 * 水平速率平方
	 * 
	 * @param entity
	 * @return
	 */
	public static final double horizontalSpeedSqr(Entity entity) {
		Vec3 dx = entity.getDeltaMovement();
		double vx = dx.x / TimeUtils.SECOND_PER_TICK;
		double vz = dx.z / TimeUtils.SECOND_PER_TICK;
		return vx * vx + vz * vz;
	}

	public static final boolean horizontalSpeedFasterThan(Entity entity, double speed) {
		return horizontalSpeedSqr(entity) > speed * speed;
	}

	/**
	 * 生成跟随实体的粒子，每tick调用
	 * 
	 * @param mob
	 * @param spwanInterval 生成每个粒子的间隔
	 * @param yOffset       生成粒子的高度偏移量
	 * @param clampRange    高斯分布随机数的截断范围
	 * @param spreadRange   生成粒子的范围
	 */
	public static final void spawnFollowParticles(Entity entity, RandomSource random, BlockParticleOption particle, int spwanInterval, double yOffset, double clampRange, double spreadRange, double speedX, double speedY, double speedZ) {
		if (entity.tickCount % spwanInterval == 0) {
			Vec3 pos = entity.position();
			double scaleFactor = spreadRange / clampRange;// 粒子的位移缩放因子
			entity.level().addParticle(
					particle,
					pos.x() + Math.clamp(random.nextGaussian(), -clampRange, clampRange) * scaleFactor, // 高斯分布做截断限制范围，中心概率大边缘概率小
					pos.y() + yOffset, // Y坐标（实体位置向下偏移，对应脚下）
					pos.z() + Math.clamp(random.nextGaussian(), -clampRange, clampRange) * scaleFactor,
					speedX,
					speedY,
					speedZ);
		}
	}

	public static final void spawnFollowParticles(LivingEntity entity, BlockParticleOption particle, int spwanInterval, double yOffset, double clampRange, double spreadRange, double speedX, double speedY, double speedZ) {
		spawnFollowParticles(entity, entity.getRandom(), particle, spwanInterval, yOffset, clampRange, spreadRange, speedX, speedY, speedZ);
	}

	public static final void spawnGroundParticles(LivingEntity entity, int spwanInterval, double yOffset, double clampRange, double spreadRange) {
		// 判断是否处于地面
		if (entity.onGround() && !entity.isInWater()) {
			BlockState groundBlock = entity.level().getBlockState(entity.blockPosition().below());// 获取实体当前脚下的方块
			if (groundBlock.getBlock() != Blocks.AIR) {
				spawnFollowParticles(entity, new BlockParticleOption(ParticleTypes.BLOCK, groundBlock), spwanInterval, yOffset, clampRange, spreadRange, 0, 0, 0);
			}
		}
	}

	/**
	 * 横扫之刃特效渲染
	 */
	public void renderSweepAttack(Entity entity) {
		if (entity.level() instanceof ServerLevel serverLevel) {
			double offsetX = (double) (-Mth.sin(entity.getYRot() * ((float) Math.PI / 180.0f)));
			double offsetZ = (double) Mth.cos(entity.getYRot() * ((float) Math.PI / 180.0f));
			serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, entity.getX() + offsetX, entity.getY(0.5), entity.getZ() + offsetZ, 0, offsetX, 0, offsetZ, 0);
		}
	}

	/**
	 * 破盾。判定参考Player。<br>
	 * 
	 * @param entity       要破盾的实体
	 * @param forceDisable 是否强制破盾
	 */
	public static final void disableShield(LivingEntity entity, boolean forceDisable) {
		// 计算全身装备的破盾抗性
		float p = 0.25f + (float) EnchantmentHelper.getBlockEfficiency(entity) * 0.05f;
		if (forceDisable) {
			p += 0.75f;// 此时p > 1.0，random.nextFloat()生成的随机数在0到1之间，判定必然破盾
		}
		// 概率破盾
		if (entity.getRandom().nextFloat() < p) {
			entity.stopUsingItem();
			entity.handleEntityEvent(EntityEvent.SHIELD_DISABLED);
		}
	}

	public static final void spawnRunningGroundParticles(LivingEntity entity, int spwanInterval, double speed) {
		if (horizontalSpeedFasterThan(entity, speed)) {
			spawnGroundParticles(entity, spwanInterval, 0.1, 10.0, 1.0);
		}
	}

	public static final float DEFAULT_TOLERANCE = 0.95F;

	/**
	 * Yaw角转方向向量
	 * 
	 * @param yaw 使用角度而非弧度
	 * @return 归一化后的水平方向
	 */
	public static final Vec3 horizontalDirection(float yaw) {
		float radYaw = (float) Math.toRadians(yaw);
		return new Vec3(-(float) Math.sin(radYaw), 0, (float) Math.cos(radYaw));// 长度为1
	}

	/**
	 * 检测头部是否朝向目标方向
	 * 
	 * @param entity
	 * @param direction      方向
	 * @param partialTick
	 * @param toleranceAngle 允许的检测范围，使用单位为角度
	 * @return
	 */
	private static boolean isHeadFacing(LivingEntity entity, Vec3 direction, float partialTick, float toleranceAngle) {
		return entity.getViewVector(partialTick).dot(direction.normalize()) >= Math.cos(Math.toRadians(toleranceAngle));
	}

	/**
	 * 检测身体是否朝向目标方向，仅判断水平方向。<br>
	 * 
	 * @param entity
	 * @param pos
	 * @param tolerance
	 * @return
	 */
	private static boolean isBodyFacingHorizontal(LivingEntity entity, Vec3 pos, float toleranceAngle) {
		return horizontalDirection(entity.yBodyRot).dot(new Vec3(pos.x(), 0, pos.z()).normalize()) >= Math.cos(Math.toRadians(toleranceAngle));
	}

	/**
	 * 判断实体朝向
	 * 
	 * @param entity
	 * @param pos
	 * @param partialTick
	 * @param toleranceAngle
	 * @return
	 */
	public static boolean isEntityFacing(LivingEntity entity, Vec3 pos, float partialTick, float toleranceAngle) {
		Vec3 direction = pos.subtract(entity.position());
		return isHeadFacing(entity, direction, partialTick, toleranceAngle) && isBodyFacingHorizontal(entity, direction, toleranceAngle);
	}

	public static boolean isEntityFacing(LivingEntity entity, Vec3 targetPos, float toleranceAngle) {
		return isEntityFacing(entity, targetPos, 1.0f, toleranceAngle);
	}
}
