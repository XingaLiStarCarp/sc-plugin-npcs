package ba.world.terrain;

import com.mojang.serialization.Codec;

import minecraft.codec.annotation.CodecAutogen;
import minecraft.codec.annotation.CodecEntry;
import minecraft.codec.annotation.IntRange;

public enum TerrainType {
	/**
	 * 市街地
	 */
	CityArea(0),

	/**
	 * 户外
	 */
	DesertArea(1),

	/**
	 * 屋内
	 */
	IndoorArea(2);

	static {
		CodecAutogen.CodecGenerator.callerCodec();
	}

	@CodecAutogen
	public static final Codec<TerrainType> CODEC = null;

	@CodecEntry(int_range = { @IntRange(min = 0, max = 2) })
	public final int id;

	private TerrainType(int id) {
		this.id = id;
	}
}
