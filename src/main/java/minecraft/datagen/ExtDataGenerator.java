package minecraft.datagen;

import java.util.concurrent.CompletableFuture;

import minecraft.core.Core;
import minecraft.datagen.annotation.ItemDatagen;
import minecraft.datagen.annotation.LangDatagen;
import minecraft.datagen.annotation.RegistryEntry;
import minecraft.datagen.internal.ExtTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class ExtDataGenerator {
	// GatherDataEvent执行实际在注册表注册之后
	public static void datagen(GatherDataEvent event) {
		Core.logInfo("ExtDataGenerator starting to datagen.");
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper helper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		// 注册表内容生成
		RegistryEntry.RegistriesProvider registriesProvider = new RegistryEntry.RegistriesProvider(output, lookupProvider);
		generator.addProvider(event.includeServer(), registriesProvider);
		// Tag及其成员生成
		ExtTagsProvider.addProvider(generator, event.includeServer(), output, registriesProvider, helper);
		// 物品数据生成
		ItemDatagen.ModelProvider.addProvider(generator, event.includeClient(), output, helper);
		// 语言文件生成
		LangDatagen.LangProvider.addProvider(generator, event.includeClient(), output);
		RegistryEntry.DeferredEntryHolderRegister.registerAll();
		Core.logInfo("Gather data complete");
	}
}
