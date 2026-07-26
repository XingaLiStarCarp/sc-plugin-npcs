package scba.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;

import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class GunUtils {

    private static final Map<String, GunTabType> TYPE_NAME_MAP = new HashMap<>();
    private static EnumMap<GunTabType, List<String>> GUN_CACHE = null;
    private static final Random RANDOM = new Random();

    // 初始化枪械类型名称映射
    static {
        for (GunTabType type : GunTabType.values()){
            TYPE_NAME_MAP.put(type.name().toLowerCase(Locale.ROOT), type);
        }
    }

    // 获取所有枪械
    public static EnumMap<GunTabType, List<String>> getAllGuns(){
        if(GUN_CACHE != null){
            return GUN_CACHE;
        }

        EnumMap<GunTabType, List<String>> map = new EnumMap<>(GunTabType.class);
        for(GunTabType type : GunTabType.values()){
            map.put(type, new ArrayList<>());
        }

        var allGuns = TimelessAPI.getAllCommonGunIndex();
        for (var entry : allGuns){
            ResourceLocation id = entry.getKey();
            CommonGunIndex index = entry.getValue();
            String typeStr = index.getType();

            GunTabType type = TYPE_NAME_MAP.get(typeStr);
            if(typeStr != null){
                map.get(type).add(id.toString());
            }

        }
        GUN_CACHE = map;
        return map;
    }

    //根据枪械类型随机获取枪械
    public static String getRandomGun(GunTabType type){
        var allGuns = getAllGuns();
        List<String> guns = allGuns.get(type);
        if(guns == null || guns.isEmpty()){
            return null;
        }

        return guns.get(RANDOM.nextInt(guns.size()));
    }
}
