package ba;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import ba.entries.dimension.kivotos.Kivotos;
import ba.entries.dimension.kivotos.KivotosHeightMap;
import ba.entries.dimension.shittim_chest.ShittimChest;
import minecraft.codec.annotation.CodecAutogen.CodecGenerator;
import minecraft.core.Core;
import minecraft.core.ExecuteIn;
import minecraft.core.ModInit;
import minecraft.datagen.annotation.LangDatagen;
import minecraft.datagen.annotation.Translation;
import minecraft.dimension.Dimensions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ModEntry.ModId)
public class ModEntry {
	public static final String ModName = "BlueArchive Extension";

	public static final String ModId = "ba";

	public static final Logger Logger = LogUtils.getLogger();

	static {
		LangDatagen.LangProvider.genLangs(Translation.EN_US, Translation.ZH_CN);
		ModInit.Initializer.callerInit();
	}

	@ModInit
	public static final void init(Dist env) {
		Logger.info("classpath: " + Core.classpath());
		Core.load(true, "ba.entries");
		Core.load(true, "scba.entries");
		Core.loadClient(true, "ba.client");// 加载客户端包
		// Core.loadClient(true, "scba.client");
		// Dimensions.removeTheNether(true);
		// Dimensions.removeTheEnd(true);
		// ExecuteIn.Server(() -> {
		// Dimensions.redirectOverworld(Kivotos.ID);
		// Logger.info("Running on server-side, overworld redirected to Kivotos");
		// });
		// ExecuteIn.Client(() -> {
		// Dimensions.redirectOverworld(ShittimChest.ID);
		// Logger.info("Running on client-side, overworld redirected to Shittim Chest");
		// });
		// Dimensions.redirectOverworld(NearEarthSpace.ID);
	}

	@ModInit(stage = ModInit.Stage.POST_INIT)
	public static final void postinit(Dist env) {
	}
}
