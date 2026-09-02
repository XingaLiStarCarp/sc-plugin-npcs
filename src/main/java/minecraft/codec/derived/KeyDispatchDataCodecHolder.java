package minecraft.codec.derived;

import net.minecraft.util.KeyDispatchDataCodec;

@SuppressWarnings("rawtypes")
public interface KeyDispatchDataCodecHolder<_CodecEntryTp> extends DerivedCodecHolder<KeyDispatchDataCodec, _CodecEntryTp> {
	@Override
	default Class<KeyDispatchDataCodec> codecClass() {
		return KeyDispatchDataCodec.class;
	}
}