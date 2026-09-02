package minecraft.client.graphics.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;

/**
 * 使用固定一种体型的玩家模型的实体渲染器。<br>
 * 参考自net.minecraft.client.renderer.entity.player.PlayerRenderer。<br>
 * 
 * @param <_T>
 */
public abstract class VallinaPlayerModelRenderer<_T extends LivingEntity> extends LivingEntityRenderer<_T, PlayerModel<_T>> {
	/**
	 * 模型地面阴影半径
	 */
	public static final float DEFAULT_SHADOW_RADIUS = 0.5f;

	public VallinaPlayerModelRenderer(EntityRendererProvider.Context context, boolean slim) {
		super(context, new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), DEFAULT_SHADOW_RADIUS);
		// 盔甲层
		this.addLayer(new HumanoidArmorLayer<>(this,
				new HumanoidArmorModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidArmorModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
				context.getModelManager()));
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		this.addLayer(new ArrowLayer<>(context, this));
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
		this.addLayer(new BeeStingerLayer<>(this));
	}

	/**
	 * 根据手持物品获取手臂姿态
	 * 
	 * @param <_T>
	 * @param entity
	 * @param hand
	 * @return
	 */
	public static final <_T extends LivingEntity> HumanoidModel.ArmPose getArmPose(_T entity, InteractionHand hand) {
		ItemStack itemstack = entity.getItemInHand(hand);
		if (itemstack.isEmpty()) {
			return HumanoidModel.ArmPose.EMPTY;
		} else {
			if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
				UseAnim useanim = itemstack.getUseAnimation();
				if (useanim == UseAnim.BLOCK) {
					return HumanoidModel.ArmPose.BLOCK;
				}

				if (useanim == UseAnim.BOW) {
					return HumanoidModel.ArmPose.BOW_AND_ARROW;
				}

				if (useanim == UseAnim.SPEAR) {
					return HumanoidModel.ArmPose.THROW_SPEAR;
				}

				if (useanim == UseAnim.CROSSBOW && hand == entity.getUsedItemHand()) {
					return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
				}

				if (useanim == UseAnim.SPYGLASS) {
					return HumanoidModel.ArmPose.SPYGLASS;
				}

				if (useanim == UseAnim.TOOT_HORN) {
					return HumanoidModel.ArmPose.TOOT_HORN;
				}

				if (useanim == UseAnim.BRUSH) {
					return HumanoidModel.ArmPose.BRUSH;
				}
			} else if (!entity.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
				return HumanoidModel.ArmPose.CROSSBOW_HOLD;
			}
			HumanoidModel.ArmPose forgeArmPose = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemstack).getArmPose(entity, hand, itemstack);
			if (forgeArmPose != null)
				return forgeArmPose;

			return HumanoidModel.ArmPose.ITEM;
		}
	}

	/**
	 * 设置模型渲染参数和姿态
	 * 
	 * @param entity
	 */
	protected void setModelProperties(_T entity) {
		PlayerModel<_T> playermodel = this.getModel();
		if (entity.isSpectator()) {
			playermodel.setAllVisible(false);
			playermodel.head.visible = true;
			playermodel.hat.visible = true;
		} else {
			playermodel.setAllVisible(true);
			playermodel.crouching = entity.isCrouching();
			HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(entity, InteractionHand.MAIN_HAND);
			HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(entity, InteractionHand.OFF_HAND);
			if (humanoidmodel$armpose.isTwoHanded()) {
				humanoidmodel$armpose1 = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
			}

			if (entity.getMainArm() == HumanoidArm.RIGHT) {
				playermodel.rightArmPose = humanoidmodel$armpose;
				playermodel.leftArmPose = humanoidmodel$armpose1;
			} else {
				playermodel.rightArmPose = humanoidmodel$armpose1;
				playermodel.leftArmPose = humanoidmodel$armpose;
			}
		}
	}

	/**
	 * 位矢增量线性插值，客户端渲染频率远高于服务端的逻辑更新的20Hz。<br>
	 * 需要插值做平滑过渡处理。<br>
	 * 
	 * @param entity
	 * @param partialTick
	 * @return
	 */
	public Vec3 getDeltaMovementLerped(_T entity, float partialTick) {
		return entity.getDeltaMovement().lerp(entity.getDeltaMovement(), (double) partialTick);
	}

	/**
	 * 动画插值的肢体旋转变换.<br>
	 * 如果没有此方法则肢体动画（包括部分Pose，例如游泳姿态）会出现渲染错位。<br>
	 * 
	 * @param entity
	 * @param poseStack
	 * @param bob
	 * @param yBodyRot
	 * @param partialTick
	 */
	@Override
	protected void setupRotations(_T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
		float f = entity.getSwimAmount(partialTick);
		float f1 = entity.getViewXRot(partialTick);
		if (entity.isFallFlying()) {
			super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
			float f2 = (float) entity.getFallFlyingTicks() + partialTick;
			float f3 = Mth.clamp(f2 * f2 / 100.0F, 0.0F, 1.0F);
			if (!entity.isAutoSpinAttack()) {
				poseStack.mulPose(Axis.XP.rotationDegrees(f3 * (-90.0F - f1)));
			}

			Vec3 vec3 = entity.getViewVector(partialTick);
			Vec3 vec31 = getDeltaMovementLerped(entity, partialTick);
			double d0 = vec31.horizontalDistanceSqr();
			double d1 = vec3.horizontalDistanceSqr();
			if (d0 > 0.0 && d1 > 0.0) {
				double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
				double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
				poseStack.mulPose(Axis.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
			}
		} else if (f > 0.0F) {
			super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
			float f4 = entity.isInWater() || entity.isInFluidType((fluidType, height) -> entity.canSwimInFluidType(fluidType)) ? -90.0F - entity.getXRot() : -90.0F;
			float f5 = Mth.lerp(f, 0.0F, f4);
			poseStack.mulPose(Axis.XP.rotationDegrees(f5));
			if (entity.isVisuallySwimming()) {
				poseStack.translate(0.0F, -1.0F, 0.3F);
			}
		} else {
			super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
		}
	}

	/**
	 * 是否渲染名字。<br>
	 * 采用MobRenderer同款判定。<br>
	 */
	@Override
	protected boolean shouldShowName(_T entity) {
		return super.shouldShowName(entity) && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
	}

	@Override
	public void render(_T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		this.setModelProperties(entity);
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
