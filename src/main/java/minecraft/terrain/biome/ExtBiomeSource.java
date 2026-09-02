package minecraft.terrain.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import minecraft.codec.annotation.AsDataField;
import minecraft.codec.annotation.CodecAutogen;
import minecraft.codec.annotation.CodecEntry;
import minecraft.codec.annotation.CodecTarget;
import minecraft.codec.annotation.CodecAutogen.CodecAutogenAttributes;
import minecraft.codec.derived.MapCodecHolder;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

@AsDataField
public abstract class ExtBiomeSource extends BiomeSource implements MapCodecHolder<BiomeSource> {
	static {
		CodecAutogen.CodecAutogenAttributes.markDerivedAutoAttributes(CodecAutogenAttributes.of(true, true, true, true));
	}

	// 派生类必须包含以下字段，它会自动注册
	// public static final MapCodec<ExtBiomeSource> CODEC = null;

	@Override
	@SuppressWarnings({ "unchecked" })
	public MapCodec<? extends BiomeSource> codec() {
		return MapCodecHolder.super.codec();
	}

	@CodecEntry
	protected List<Holder<Biome>> possible_biomes;

	/**
	 * 仅数据生成时序列化使用
	 * 
	 * @param context
	 * @param possibleBiomeKeys
	 */
	public ExtBiomeSource(BootstrapContext<?> context, List<String> possibleBiomeKeys) {
		MapCodecHolder.super.construct(BiomeSource.class);
		resolvePossibleBiomes(context, possibleBiomeKeys);
	}

	public ExtBiomeSource(BootstrapContext<?> context, String... possibleBiomeKeys) {
		this(context, List.of(possibleBiomeKeys));
	}

	/**
	 * 实际运行时反序列化使用
	 * 
	 * @param possibleBiomesList
	 */
	@CodecTarget
	public ExtBiomeSource(List<Holder<Biome>> possibleBiomesList) {
		MapCodecHolder.super.construct(BiomeSource.class);
		this.possible_biomes = possibleBiomesList;
	}

	/**
	 * 从子类中读取possibleBiomeHolders列表，如果为null则根据possibleBiomeKeys创建。
	 * 
	 * @param bootstrapContext
	 * @param possibleBiomeKeys 可能的生物群系的key列表，如果为null则扫描目标对象的静态List<String>字段
	 */
	private void resolvePossibleBiomes(BootstrapContext<?> bootstrapContext, List<String> possibleBiomeKeys) {
		if (possible_biomes == null) {
			possible_biomes = new ArrayList<>();
			for (String key : possibleBiomeKeys) {
				// datagenStageHolder()的内部值可以是null，datagen只要ResourceKey即可，例如Reference{ResourceKey[minecraft:worldgen/biome / minecraft:forest]=null}就是合法的
				// 此时获取到的Holder都是unbound状态，但不影响数据生成
				possible_biomes.add(ExtBiome.datagenStageHolder(bootstrapContext, key));
			}
		}
	}

	/**
	 * 该方法返回的结果将在MC中缓存，理论上每次进入世界只调用一次，因此本类不再缓存Stream。<br>
	 * 仅运行时调用，且无法在运行时访问MappedRegistry，必须通过datagen就确定好所有可能的生物群系。
	 */
	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return this.possible_biomes.parallelStream();
	}

	/**
	 * 获取任意一个生物群系的Holder
	 * 
	 * @param key
	 * @return
	 */
	protected static final Holder<Biome> biome(String key) {
		return ExtBiome.getBiome(key);
	}
}
