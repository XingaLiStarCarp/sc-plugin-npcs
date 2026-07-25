package minecraft.extended.tacz;

import java.lang.reflect.Field;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.LivingEntityAmmoCheck;

import jvmsp.reflection;
import jvmsp.unsafe;
import minecraft.extended.gun.GunOperator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import scba.ModEntry;

public class TaczGunOperator implements GunOperator {
	/**
	 * 通过Unsafe修改LivingEntityMixin的tacz$ammoCheck字段实现自定义
	 */
	public static class CustomLivingEntityAmmoCheck extends LivingEntityAmmoCheck {
		private boolean needCheckAmmo;
		private boolean consumesAmmoOrNot;

		public CustomLivingEntityAmmoCheck(boolean needCheckAmmo, boolean consumesAmmoOrNot) {
			super(null);
			this.needCheckAmmo = needCheckAmmo;
			this.consumesAmmoOrNot = consumesAmmoOrNot;
		}

		@Override
		public boolean needCheckAmmo() {
			return needCheckAmmo;
		}

		@Override
		public boolean consumesAmmoOrNot() {
			return consumesAmmoOrNot;
		}

		/**
		 * 设置换弹时是否检查有弹药夹
		 * 
		 * @param needCheckAmmo
		 */
		public void setReloadNeedCheckAmmo(boolean needCheckAmmo) {
			this.needCheckAmmo = needCheckAmmo;
		}

		/**
		 * 设置射击时是否消耗弹药
		 * 
		 * @param consumesAmmoOrNot
		 */
		public void setShootConsumesAmmo(boolean consumesAmmoOrNot) {
			this.consumesAmmoOrNot = consumesAmmoOrNot;
		}

		private static Field LivingEntity_tacz$ammoCheck;

		static {
			// tacz的字段是通过Mixin类LivingEntity添加的
			LivingEntity_tacz$ammoCheck = reflection.find_declared_field(LivingEntity.class, "tacz$ammoCheck");
		}

		public static final void setLivingEntityAmmoCheck(IGunOperator entity, LivingEntityAmmoCheck ammoCheck) {
			unsafe.write(entity, LivingEntity_tacz$ammoCheck, ammoCheck);
		}
	}

	protected final LivingEntity entity;
	protected final IGunOperator gunOperator;
	private final CustomLivingEntityAmmoCheck tacz$ammoCheck;

	protected InteractionHand hand;

	public TaczGunOperator(LivingEntity entity, InteractionHand hand) {
		this.entity = entity;
		this.gunOperator = IGunOperator.fromLivingEntity(entity);
		this.hand = hand;
		this.tacz$ammoCheck = new CustomLivingEntityAmmoCheck(true, true);
		CustomLivingEntityAmmoCheck.setLivingEntityAmmoCheck(gunOperator, tacz$ammoCheck);
	}

	public TaczGunOperator(LivingEntity shooter) {
		this(shooter, InteractionHand.MAIN_HAND);
	}

	@Override
	public InteractionHand getGunHand() {
		return this.hand;
	}

	@Override
	public LivingEntity getGunHolder() {
		return this.entity;
	}

	@Override
	public final void aim(boolean isAim) {
		this.gunOperator.aim(isAim);
	}

	@Override
	public final void melee() {
		this.gunOperator.melee();
	}

	public final void draw() {
		gunOperator.draw(() -> this.getGunItem());
	}

	@Override
	public final void bolt() {
		gunOperator.bolt();
	}

	@Override
	public final void reload() {
		gunOperator.reload();
	}

	@Override
	public final int getGunAmmo() {
		return TaczGuns.getGunAmmo(this.getGunItem());
	}

	/**
	 * 获取手中的枪
	 * 
	 * @return
	 */
	public final IGun getGun() {
		return IGun.getIGunOrNull(getGunItem());
	}

	/**
	 * 向指定坐标射击
	 * 
	 * @param hand
	 * @param target
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final ShootResult shoot(Vec3 target) {
		if (this.getGun() == null) {
			return ShootResult.NOT_GUN;
		} else {
			double x = target.x - this.entity.getX();
			double y = target.y - this.entity.getEyeY();
			double z = target.z - this.entity.getZ();
			float yaw = (float) -Math.toDegrees(Math.atan2(x, z));
			float pitch = (float) -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z)));
			try {
				return gunOperator.shoot(() -> pitch, () -> yaw);// 防止自定义枪包抛错
			} catch (Throwable ex) {
				ModEntry.LOGGER.error(this.entity + " gun attack failed", ex);
				return ShootResult.UNKNOWN_FAIL;
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public final ShootResult shootAuto(Vec3 target) {
		ShootResult result = GunOperator.super.shootAuto(target);
		switch (result) {
		case ShootResult.NOT_DRAW:
			this.draw();
			break;
		case ShootResult.NEED_BOLT:
			this.bolt();
			break;
		case ShootResult.NO_AMMO:
			this.reload();
			break;
		default:
			break;
		}
		return result;
	}

	@Override
	public void setReloadNeedCheckAmmo(boolean needCheckAmmo) {
		this.tacz$ammoCheck.setReloadNeedCheckAmmo(needCheckAmmo);
	}

	@Override
	public void setShootConsumesAmmo(boolean consumesAmmoOrNot) {
		this.tacz$ammoCheck.setShootConsumesAmmo(consumesAmmoOrNot);
	}

	public void crawl(boolean isCrawl) {
		gunOperator.crawl(isCrawl);
	}

	@Override
	public boolean isGun(ItemStack item) {
		return TaczGuns.isGun(item);
	}
}
