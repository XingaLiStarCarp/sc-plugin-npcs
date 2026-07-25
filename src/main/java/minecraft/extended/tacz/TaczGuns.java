package minecraft.extended.tacz;

import java.util.Optional;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TaczGuns {
	/**
	 * 构造一把枪械
	 * 
	 * @param gunId        枪械ID
	 * @param ammoCount    装载的弹药数
	 * @param fireMode     开火模式
	 * @param ammoInBarrel 是否有子弹已经上膛
	 * @return
	 */
	public static final ItemStack getGun(ResourceLocation gunId, int ammoCount, FireMode fireMode, boolean ammoInBarrel) {
		return GunItemBuilder.create()
				.setId(gunId)
				.setAmmoCount(ammoCount)
				.setFireMode(fireMode)
				.setAmmoInBarrel(ammoInBarrel)
				.build();
	}

	public static final ItemStack getGun(String gunId, int ammoCount, FireMode fireMode, boolean ammoInBarrel) {
		return getGun(ResourceLocation.parse(gunId), ammoCount, fireMode, ammoInBarrel);
	}

	/**
	 * 获取枪械的数据
	 * 
	 * @param gunId
	 * @return
	 */
	public static final GunData getGunData(ResourceLocation gunId) {
		Optional<CommonGunIndex> idx = TimelessAPI.getCommonGunIndex(gunId);
		if (idx.isEmpty()) {
			return null;
		} else {
			return idx.get().getGunData();
		}
	}

	public static final GunData getGunData(String gunId) {
		return getGunData(ResourceLocation.parse(gunId));
	}

	/**
	 * 返回一把默认弹药数的未上膛的枪
	 * 
	 * @param gunId
	 * @return
	 */
	public static final ItemStack getGun(String gunId) {
		ResourceLocation id = ResourceLocation.parse(gunId);
		GunData data = getGunData(id);
		return getGun(id, data.getAmmoAmount(), data.getFireModeSet().getFirst(), false);
	}

	/**
	 * 获取当前枪械的弹药数
	 * 
	 * @param gunItem
	 * @return
	 */
	public static final int getGunAmmo(ItemStack gunItem) {
		return ((GunItemDataAccessor) gunItem.getItem()).getCurrentAmmoCount(gunItem);
	}

	public static final boolean isGun(ItemStack item) {
		return item.getItem() instanceof ModernKineticGunItem;
	}
}
