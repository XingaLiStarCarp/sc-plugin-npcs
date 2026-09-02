package minecraft.codec.derived;

import com.mojang.serialization.Codec;

@SuppressWarnings("rawtypes")
public interface CodecHolder<_CodecEntryTp> extends DerivedCodecHolder<Codec, _CodecEntryTp> {
	@Override
	default Class<Codec> codecClass() {
		return Codec.class;
	}
}
