package minecraft.entity.player;

import java.lang.reflect.Field;

import sys.jvm.reflection;
import sys.jvm.unsafe;
import minecraft.LogicalEnd;
import minecraft.entity.data.EntityData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PlayerData {
	static Field AbstractClientPlayer_deltaMovementOnPreviousTick;
	static Field LocalPlayer_usingItemHand;
	static Field LocalPlayer_crouching;

	static {
		if (LogicalEnd.isClient()) {
			AbstractClientPlayer_deltaMovementOnPreviousTick = reflection.find_declared_field(AbstractClientPlayer.class, "deltaMovementOnPreviousTick");
			LocalPlayer_usingItemHand = reflection.find_declared_field(LocalPlayer.class, "usingItemHand");
			LocalPlayer_crouching = reflection.find_declared_field(LocalPlayer.class, "crouching");
		}
	}

	public static final void setDeltaMovementOnPreviousTick(Player localPlayer, Vec3 prevdx) {
		unsafe.write(localPlayer, AbstractClientPlayer_deltaMovementOnPreviousTick, prevdx);
	}

	public static final void setUsingItemHand(Player localPlayer, InteractionHand usingItemHand) {
		unsafe.write(localPlayer, LocalPlayer_usingItemHand, usingItemHand);
	}

	public static final void setCrouching(Player localPlayer, boolean crouching) {
		unsafe.write(localPlayer, LocalPlayer_crouching, crouching);
	}

	public static final void syncEntityData(Entity srcEntity, Player destEntity) {
		EntityData.syncEntityData(srcEntity, destEntity);// 同步内存字段值
		// 同步deltaMovementOnPreviousTick，此值将用于玩家的动画插值
		PlayerData.setDeltaMovementOnPreviousTick(destEntity, srcEntity.getDeltaMovement());
		PlayerData.setCrouching(destEntity, srcEntity.isCrouching());
		if (srcEntity instanceof LivingEntity srcLivingEntity) {
			PlayerData.setUsingItemHand(destEntity, srcLivingEntity.getUsedItemHand());
			destEntity.setItemSlot(EquipmentSlot.MAINHAND, srcLivingEntity.getMainHandItem());
			destEntity.setItemSlot(EquipmentSlot.OFFHAND, srcLivingEntity.getOffhandItem());
			destEntity.setItemSlot(EquipmentSlot.HEAD, srcLivingEntity.getItemBySlot(EquipmentSlot.HEAD));
			destEntity.setItemSlot(EquipmentSlot.CHEST, srcLivingEntity.getItemBySlot(EquipmentSlot.CHEST));
			destEntity.setItemSlot(EquipmentSlot.LEGS, srcLivingEntity.getItemBySlot(EquipmentSlot.LEGS));
			destEntity.setItemSlot(EquipmentSlot.FEET, srcLivingEntity.getItemBySlot(EquipmentSlot.FEET));
		}
	}

}
