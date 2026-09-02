package minecraft.codec.derived;

import com.mojang.serialization.MapCodec;

@SuppressWarnings("rawtypes")
public interface MapCodecHolder<_CodecEntryTp> extends DerivedCodecHolder<MapCodec, _CodecEntryTp> {
	@Override
	default Class<MapCodec> codecClass() {
		return MapCodec.class;
	}
}