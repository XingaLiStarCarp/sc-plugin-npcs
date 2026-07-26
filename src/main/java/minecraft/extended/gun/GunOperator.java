package minecraft.extended.gun;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiFunction;

import minecraft.extended.tacz.TaczGunOperator;
import minecraft.extended.tacz.TaczGuns;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface GunOperator {
	static final Random SPREAD_RANDOM = new Random();

	/**
	 * 射击目标点散布
	 * 
	 * @param target
	 * @param currentPos
	 * @param spread
	 * @return
	 */
	public static Vec3 getRandomPointInSpread(Vec3 target, Vec3 currentPos, double spread) {
		Vec3 delta = new Vec3(target.x - currentPos.x, target.y - currentPos.y, target.z - currentPos.z);
		double distance = Math.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
		Vec3 direction = new Vec3(delta.x / distance, delta.y / distance, delta.z / distance);
		double spreadRad = Math.toRadians(spread);
		// 1. 随机水平旋转角度 (0 ~ 2PI)
		double theta = SPREAD_RANDOM.nextDouble() * 2 * Math.PI;
		// 2. 随机偏转半径 (0 ~ 1)，使用 sqrt 保证在圆内分布均匀
		// 如果希望中心更密集（高斯分布），可以改用其他的随机算法
		double randomRadius = Math.sqrt(SPREAD_RANDOM.nextDouble());
		// 3. 计算实际的随机偏转角度 (0 ~ spreadRad)
		double angle = spreadRad * randomRadius;
		// 创建一个垂直于原始方向的随机向量
		Vec3 temp;
		if (Math.abs(direction.x) > 0.9) {
			temp = new Vec3(0, 1, 0);
		} else {
			temp = new Vec3(1, 0, 0);
		}
		// 计算叉积得到第一个正交向量
		Vec3 u = direction.cross(temp).normalize();
		// 计算叉积得到第二个正交向量
		Vec3 v = direction.cross(u);
		double dx = direction.x * Math.cos(angle) +
				u.x * Math.sin(angle) * Math.cos(theta) +
				v.x * Math.sin(angle) * Math.sin(theta);
		double dy = direction.y * Math.cos(angle) +
				u.y * Math.sin(angle) * Math.cos(theta) +
				v.y * Math.sin(angle) * Math.sin(theta);
		double dz = direction.z * Math.cos(angle) +
				u.z * Math.sin(angle) * Math.cos(theta) +
				v.z * Math.sin(angle) * Math.sin(theta);
		return new Vec3(currentPos.x + dx * distance, currentPos.y + dy * distance, currentPos.z + dz * distance);
	}

	public static final int INVALID_GUN_OPERATOR_TYPE = -1;

	/**
	 * 瞄准动作
	 * 
	 * @param isAim
	 */
	public default void aim(boolean isAim) {

	}

	/**
	 * 使用枪械近战攻击
	 */
	public default void melee() {

	}

	/**
	 * 拉栓上膛
	 */
	public default void bolt() {

	}

	/**
	 * 换弹
	 */
	public abstract void reload();

	/**
	 * 获取枪械当前剩余子弹
	 * 
	 * @return
	 */
	public abstract int getGunAmmo();

	/**
	 * 获取持枪的手
	 * 
	 * @return
	 */
	public abstract InteractionHand getGunHand();

	/**
	 * 获取持枪者
	 * 
	 * @return
	 */
	public abstract LivingEntity getGunHolder();

	/**
	 * 获取枪械物品
	 * 
	 * @return
	 */
	public default ItemStack getGunItem() {
		return getGunHolder().getItemInHand(getGunHand());
	}

	/**
	 * 朝指定坐标射击，需要手动换弹或拉栓
	 * 提供空默认实现是考虑到一些锁头武器不接受坐标输入，只接受实体作为目标
	 * 
	 * @param <_Result>
	 * @param target
	 * @return
	 */
	public default <_Result> _Result shoot(Vec3 target) {
		return null;
	}

	public default <_Result> _Result shoot(Vec3 target, double spread) {
		return shoot(getRandomPointInSpread(target, getGunHolder().getEyePosition(), spread));
	}

	public default <_Result> _Result shoot(Entity target) {
		return this.shoot(target.position());
	}

	/**
	 * 朝指定坐标射击，但不需要人工去换弹或拉栓，方法内在无弹药时自动换弹
	 * 
	 * @param <_Result>
	 * @param target
	 * @return
	 */
	public default <_Result> _Result shootAuto(Vec3 target) {
		return null;
	}

	public default <_Result> _Result shootAuto(Vec3 target, double spread) {
		return shootAuto(getRandomPointInSpread(target, getGunHolder().getEyePosition(), spread));
	}

	public default <_Result> _Result shootAuto(Entity target) {
		return this.shootAuto(target.position());
	}

	public default void setReloadNeedCheckAmmo(boolean needCheckAmmo) {

	}

	public default void setShootConsumesAmmo(boolean consumesAmmoOrNot) {

	}

	/**
	 * 判断是否是枪
	 * 
	 * @param item
	 * @return
	 */
	public abstract boolean isGun(ItemStack item);

	/**
	 * 通用持枪操作API
	 */
	public class GeneralGunOperator implements GunOperator {
		// 不同枪械mod的GunOperator接口的构造函数列表
		private static final ArrayList<BiFunction<LivingEntity, InteractionHand, ? extends GunOperator>> GUN_OPS = new ArrayList<>();

		/**
		 * 注册一种GunOperator，使其能被GeneralGunOperator使用
		 * 
		 * @param opCtor
		 * @return
		 */
		public static final void register(BiFunction<LivingEntity, InteractionHand, ? extends GunOperator> opCtor) {
			GUN_OPS.add(opCtor);
		}

		public static final int registeredGuns() {
			return GUN_OPS.size();
		}

		static {
			// 自带支持的枪械
			GeneralGunOperator.register(TaczGunOperator::new);
		}

		protected final GunOperator[] gunOps;

		public GeneralGunOperator(LivingEntity entity, InteractionHand hand) {
			int gunOpNum = registeredGuns();
			if (gunOpNum > 0) {
				// 为每个枪械mod都创建对应的射击接口
				this.gunOps = new GunOperator[gunOpNum];
				for (int idx = 0; idx < gunOpNum; ++idx) {
					this.gunOps[idx] = GUN_OPS.get(idx).apply(entity, hand);
				}
			} else {
				throw new java.lang.InstantiationError("at least 1 gun operator should be registered");
			}
		}

		public GeneralGunOperator(LivingEntity shooter) {
			this(shooter, InteractionHand.MAIN_HAND);
		}

		protected final GunOperator currentGunOperator() {
			int type = this.getGunOperatorType();
			if (type != INVALID_GUN_OPERATOR_TYPE) {
				return gunOps[type];
			} else {
				return null;
			}
		}

		@Override
		public InteractionHand getGunHand() {
			return gunOps[0].getGunHand();
		}

		@Override
		public LivingEntity getGunHolder() {
			return gunOps[0].getGunHolder();
		}

		/**
		 * 根据当前主手持枪，获取对应的mod的枪械射击接口类型
		 * 
		 * @return
		 */
		public final int getGunOperatorType() {
			ItemStack gunItem = this.getGunItem();
			for (int idx = 0; idx < gunOps.length; ++idx) {
				GunOperator op = gunOps[idx];
				if (op.isGun(gunItem))
					return idx;
			}
			return INVALID_GUN_OPERATOR_TYPE;
		}

		@Override
		public final boolean isGun(ItemStack item) {
			ItemStack gunItem = this.getGunItem();
			for (int idx = 0; idx < gunOps.length; ++idx) {
				GunOperator op = gunOps[idx];
				if (op.isGun(gunItem))
					return true;
			}
			return false;
		}

		@Override
		public void aim(boolean isAim) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				op.aim(isAim);
			}
		}

		@Override
		public void melee() {
			GunOperator op = currentGunOperator();
			if (op != null) {
				op.melee();
			}
		}

		@Override
		public void bolt() {
			GunOperator op = currentGunOperator();
			if (op != null) {
				op.bolt();
			}
		}

		@Override
		public void reload() {
			GunOperator op = currentGunOperator();
			if (op != null) {
				op.reload();
			}
		}

		@Override
		public int getGunAmmo() {
			GunOperator op = currentGunOperator();
			if (op != null) {
				return op.getGunAmmo();
			}
			return 0;
		}

		@Override
		public <_Result> _Result shoot(Vec3 target) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				return op.shoot(target);
			}
			return null;
		}

		@Override
		public <_Result> _Result shoot(Vec3 target, double spread) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				return op.shoot(target, spread);
			}
			return null;
		}

		@Override
		public <_Result> _Result shoot(Entity target) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				return op.shoot(target);
			}
			return null;
		}

		@Override
		public <_Result> _Result shootAuto(Vec3 target) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				return op.shootAuto(target);
			}
			return null;
		}

		@Override
		public <_Result> _Result shootAuto(Vec3 target, double spread) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				return op.shootAuto(target, spread);
			}
			return null;
		}

		@Override
		public <_Result> _Result shootAuto(Entity target) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				return op.shootAuto(target);
			}
			return null;
		}

		@Override
		public void setReloadNeedCheckAmmo(boolean needCheckAmmo) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				op.setReloadNeedCheckAmmo(needCheckAmmo);
			}
		}

		@Override
		public void setShootConsumesAmmo(boolean consumesAmmoOrNot) {
			GunOperator op = currentGunOperator();
			if (op != null) {
				op.setShootConsumesAmmo(consumesAmmoOrNot);
			}
		}
	}

	/**
	 * 分配一把新枪
	 * 
	 * @param gun
	 * @return
	 */
	public static ItemStack newGun(String gun) {
		return TaczGuns.getGun(gun);
	}
}
