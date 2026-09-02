package minecraft.codec.derived;

import minecraft.codec.CodecAccess;
import sys.jvm.type.java_type.base;

/**
 * implements此接口标记实现此接口的类的子类中需要有静态CODEC<br>
 * 实现此接口的构造函数必须调用construct()方法以实例化储存字段的对象！
 * 
 * @param <_CodecTp>
 * @param <_CodecEntryTp>
 */
public interface DerivedCodecHolder<_CodecTp, _CodecEntryTp> extends base<DerivedCodecHolder.DerivedCodec<_CodecTp, _CodecEntryTp>> {
	public default _CodecTp codec() {
		return this.definition().codec(this);
	}

	abstract Class<_CodecTp> codecClass();

	public default void construct(Class<_CodecEntryTp> codecTypeClass) {
		this.construct(DerivedCodecHolder.DerivedCodec.class, new Class<?>[] { Class.class, Class.class }, codecClass(), codecTypeClass);
	}

	class DerivedCodec<_CodecTp, _CodecEntryTp> extends base.definition<DerivedCodecHolder<_CodecTp, _CodecEntryTp>> {
		/**
		 * 子类的CODEC
		 */
		protected _CodecTp derivedCodec;
		/**
		 * MapCodec.class、Codec.class等
		 */
		protected Class<_CodecTp> codecClass;
		/**
		 * CODEC泛型参数Class
		 */
		protected Class<_CodecEntryTp> codecTypeClass;

		public DerivedCodec(Class<_CodecTp> codecClass, Class<_CodecEntryTp> codecTypeClass) {
			this.codecClass = codecClass;
			this.codecTypeClass = codecTypeClass;
		}

		/**
		 * 在子类中搜寻MapCodec<br>
		 * 数据生成和运行时均会调用，需要保证这两个阶段均构建好CODEC。
		 */
		@SuppressWarnings("unchecked")
		public _CodecTp codec(DerivedCodecHolder<_CodecTp, _CodecEntryTp> holder) {
			if (derivedCodec == null)
				derivedCodec = (_CodecTp) CodecAccess.accessStatic(holder.getClass(), codecClass, codecTypeClass);
			return derivedCodec;
		}
	}
}
