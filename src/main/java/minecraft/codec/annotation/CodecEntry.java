package minecraft.codec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import minecraft.codec.CodecResolver;
import minecraft.codec.Codecs;
import minecraft.core.Core;
import sys.jvm.reflection;
import sys.jvm.unsafe;

/**
 * 用于标记需要序列化的字段，字段的声明顺序、类型需要和构造函数的参数顺序、类型严格保持一致。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface CodecEntry {
	public static final String pathSeparator = "/";

	/**
	 * 默认字段名称就是key名称
	 */
	public static final String fieldName = "${fieldName}";

	/**
	 * CODEC的key
	 * 
	 * @return
	 */
	String key() default fieldName;

	/**
	 * 该键的路径，使用pathSeparator分割，用于创建嵌套
	 * 
	 * @return
	 */
	String path() default "";

	/**
	 * 是否是可选字段
	 * 
	 * @return
	 */
	CodecResolver.CodecInfo.RequirementType type() default CodecResolver.CodecInfo.RequirementType.DEFAULT;

	/**
	 * 当未找到该注解字段类型的CODEC时，是否采用默认的空MapCodec
	 * 
	 * @return
	 */
	boolean empty_codec_if_not_exist() default false;

	/**
	 * 附带参数
	 * 
	 * @return
	 */
	IntRange[] int_range() default {};

	FloatRange[] float_range() default {};

	/**
	 * 限制double值范围
	 */
	DoubleRange[] double_range() default {};

	/**
	 * 字符串长度
	 */
	StringLength[] string_length() default {};

	/**
	 * List尺寸
	 * 
	 * @return
	 */
	ListSize[] list_size() default {};

	/**
	 * 返回拓展类型的Codec，包括List、Optional和自定义类的自定义CODEC。
	 * 
	 * @param f
	 * @param params
	 * @return
	 */

	public static class Resolver {
		public static final MapCodec<?> resolveFieldFinalCodec(Field f, CodecEntry params) {
			Class<?> field_type = f.getType();
			String key = params.key();
			if (key.equals(CodecEntry.fieldName))
				key = f.getName();
			CodecResolver.CodecInfo entry = Codecs.getCodec(field_type, f.getGenericType(), CodecResolver.CodecType.DEFAULT, params.empty_codec_if_not_exist(), params);
			CodecResolver.CodecInfo.RequirementType fc_type = CodecResolver.CodecInfo.RequirementType.resolve(params.type(), entry.type);// 是否已经绑定了字段名称
			Codec<?> field_codec = Codecs.asCodec(entry.codec);
			if (field_codec == null)
				throw new IllegalArgumentException("No CODEC found for type " + field_type + " in field " + f);
			String[] namespaces = params.path() == null ? new String[0] : params.path().split(CodecEntry.pathSeparator);
			for (int depth = 0; depth < namespaces.length; ++depth) {// 构建key的路径path，不包括key本身
				String namespace = namespaces[depth];
				if ("".equals(namespace.trim()))
					continue;// 略过空路径
				else
					field_codec = field_codec.fieldOf(namespace).codec();// 根据路径构造子键
			}
			MapCodec<?> final_codec = null;
			switch (fc_type) {
			case OPTINAL_NOERR:
				final_codec = field_codec.lenientOptionalFieldOf(key);
				break;
			case OPTIONAL:
				final_codec = field_codec.optionalFieldOf(key);
				break;
			case REQUIRED:
			default:
				final_codec = field_codec.fieldOf(key);
				break;
			}
			return final_codec;
		}

		/**
		 * 收集目标类中的注解有CodecEntry的字段
		 * 
		 * @param targetClass
		 * @param entries
		 * @param arg_types
		 * @param include_base 是否包含其超类的字段
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static final void collectCodecEntries(Class<?> targetClass, List<App> entries, ArrayList<Class<?>> arg_types, boolean include_base) {
			if (include_base) {
				Class<?> superCls = targetClass.getSuperclass();
				if (superCls != Object.class)
					collectCodecEntries(superCls, entries, arg_types, include_base);
			}
			reflection.class_operation.walk_fields(targetClass, CodecEntry.class, (Field f, boolean isStatic, Object value, CodecEntry annotation) -> {
				if (!isStatic) {// 只有非静态成员变量需要序列化
					final Class<?> field_type = f.getType();
					arg_types.add(field_type);// 储存该字段的类型，该类型必须和构造函数的参数类型和顺序严格匹配
					MapCodec final_codec = CodecEntry.Resolver.resolveFieldFinalCodec(f, annotation);
					Core.logInfo("Found CODEC for @CodecEntry {} {} -> {}", field_type.getSimpleName(), f.getName(), final_codec);
					entries.add(final_codec.forGetter((Object targetObj) -> {
						// 序列化时从该子句读取对象的值，并返回
						if (field_type == boolean.class) {
							return unsafe.read_member_bool(targetObj, f);
						} else if (field_type == byte.class) {
							return unsafe.read_member_byte(targetObj, f);
						} else if (field_type == short.class) {
							return unsafe.read_member_short(targetObj, f);
						} else if (field_type == char.class) {
							return unsafe.read_member_char(targetObj, f);
						} else if (field_type == int.class) {
							return unsafe.read_member_int(targetObj, f);
						} else if (field_type == long.class) {
							return unsafe.read_member_long(targetObj, f);
						} else if (field_type == float.class) {
							return unsafe.read_member_float(targetObj, f);
						} else if (field_type == double.class) {
							return unsafe.read_member_double(targetObj, f);
						} else {
							return unsafe.read_member_reference(targetObj, f);
						}
					}));
				}
				return true;
			});
		}
	}
}
