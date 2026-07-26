package scba.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;

import minecraft.extended.tacz.TaczGuns;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GunUtils {

	private static final Map<String, GunTabType> TYPE_NAME_MAP = new HashMap<>();
	private static EnumMap<GunTabType, List<String>> GUN_CACHE = null;
	private static Map<AttachmentType, List<String>> ATTACHMENT_CACHE = null;

	private static final Random RANDOM = new Random();

	// 初始化枪械类型名称映射
	static {
		for (GunTabType type : GunTabType.values()) {
			TYPE_NAME_MAP.put(type.name().toLowerCase(Locale.ROOT), type);
		}
	}

	// 获取所有枪械
	public static EnumMap<GunTabType, List<String>> getAllGuns() {
		if (GUN_CACHE != null) {
			return GUN_CACHE;
		}

		EnumMap<GunTabType, List<String>> map = new EnumMap<>(GunTabType.class);
		for (GunTabType type : GunTabType.values()) {
			map.put(type, new ArrayList<>());
		}

		var allGuns = TimelessAPI.getAllCommonGunIndex();
		for (var entry : allGuns) {
			ResourceLocation id = entry.getKey();
			CommonGunIndex index = entry.getValue();
			String typeStr = index.getType();

			GunTabType type = TYPE_NAME_MAP.get(typeStr);
			if (type != null) {
				map.get(type).add(id.toString());
			}

		}
		GUN_CACHE = map;
		return map;
	}

	// 根据枪械类型随机获取枪械
	public static String getRandomGun(GunTabType type) {
		var allGuns = getAllGuns();
		List<String> guns = allGuns.get(type);
		if (guns == null || guns.isEmpty()) {
			return null;
		}

		return guns.get(RANDOM.nextInt(guns.size()));
	}

	public static Optional<GunData> getGunData(ResourceLocation gunId) {
		return TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getGunData);
	}

	public static List<AttachmentType> getAllowedAttachmentTypes(ResourceLocation gunId) {
		return getGunData(gunId).map(GunData::getAllowAttachments).orElse(Collections.emptyList());
	}

	public static Map<AttachmentType, List<String>> getAvailableAttachments(ResourceLocation gunId) {
		List<AttachmentType> allowedTypes = getAllowedAttachmentTypes(gunId);
		if (allowedTypes.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<String> exclusiveIds = new HashSet<>();
		getGunData(gunId).ifPresent(data -> {
			data.getExclusiveAttachments().keySet().forEach(id -> exclusiveIds.add(id.toString()));
		});

		Map<AttachmentType, List<String>> result = new EnumMap<>(AttachmentType.class);
		for (AttachmentType type : allowedTypes) {
			result.put(type, new ArrayList<>());
		}

		var allAttachments = TimelessAPI.getAllCommonAttachmentIndex();
		for (var entry : allAttachments) {
			ResourceLocation id = entry.getKey();
			CommonAttachmentIndex index = entry.getValue();
			AttachmentType type = index.getType();

			if (!allowedTypes.contains(type))
				continue;

			if (!exclusiveIds.isEmpty() && !exclusiveIds.contains(id.toString()))
				continue;

			result.get(type).add(id.toString());
		}

		return result;
	}

	public static void equipRandomAttachments(ItemStack gunStack, ResourceLocation gunId) {
		var available = getAvailableAttachments(gunId);
		if (available.isEmpty())
			return;

		CompoundTag gunTag = gunStack.getOrCreateTag();

		Map<AttachmentType, String> nbtKeyMap = Map.of(
				AttachmentType.SCOPE, "AttachmentSCOPE",
				AttachmentType.EXTENDED_MAG, "AttachmentEXTENDED_MAG",
				AttachmentType.MUZZLE, "AttachmentMUZZLE",
				AttachmentType.GRIP, "AttachmentGRIP",
				AttachmentType.STOCK, "AttachmentSTOCK");

		for (Map.Entry<AttachmentType, List<String>> entry : available.entrySet()) {
			AttachmentType type = entry.getKey();
			List<String> attachments = entry.getValue();
			if (attachments.isEmpty())
				continue;

			if (RANDOM.nextBoolean()) {
				String selectedId = attachments.get(RANDOM.nextInt(attachments.size()));
				String nbtKey = nbtKeyMap.get(type);
				if (nbtKey != null) {
					gunTag.put(nbtKey, buildAttachmentNBT(selectedId));
				}
			}
		}
	}

	private static CompoundTag buildAttachmentNBT(String attachmentId) {
		CompoundTag outer = new CompoundTag();
		outer.putString("id", "tacz:attachment");
		outer.putByte("Count", (byte) 1);

		CompoundTag inner = new CompoundTag();
		inner.putString("AttachmentId", attachmentId);
		outer.put("tag", inner);

		return outer;
	}

	/**
	 * 构造指定类型的随机ID的枪（附带随机配件）
	 * 
	 * @param gun
	 * @return
	 */
	public static ItemStack getRandomGunWithAttachments(GunTabType type) {
		String gun = getRandomGun(type);
		if (gun == null)
			return ItemStack.EMPTY;
		return getRandomGunWithAttachments(gun);
	}

	/**
	 * 构造指定ID的枪（附带随机配件）
	 * 
	 * @param gun
	 * @return
	 */
	public static ItemStack getRandomGunWithAttachments(String gun) {
		ItemStack gunStack = TaczGuns.getGun(gun);
		equipRandomAttachments(gunStack, ResourceLocation.parse(gun));
		return gunStack;
	}
}
