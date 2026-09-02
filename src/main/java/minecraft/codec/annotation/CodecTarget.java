package minecraft.codec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import sys.jvm.reflection;
import sys.jvm.type.java_type;

/**
 * 辅助注解，用于标记将作为CODEC反序列化时使用的构造函数。<br>
 * 具有该注解的构造函数将在调用CodecAutogen.CodecGenerator.forCodec()时检查传入的ctorTypes参数类型是否和本构造函数的参数类型匹配，<br>
 * 一个类只能有一个构造函数具有此标记，若有多个构造函数具有此标记则抛出异常。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR })
public @interface CodecTarget {

	/**
	 * 当ctorTypes为空时，是否强制匹配构造函数的参数列表也为空。<br>
	 * 默认为false，即传入ctorTypes为空时将在CodecAutogen.CodecGenerator.generate()生成CODEC阶段自动查找匹配的构造函数。<br>
	 * 若设置为true，即便存在匹配的未标记CodecTarget的构造函数，也会立即报错。
	 * 
	 * @return
	 */
	boolean instant_check() default false;

	/**
	 * 仅当instant_check为true时生效。<br>
	 * 开启后若匹配空构造函数失败则会抛出异常中断程序。<br>
	 * 
	 * @return
	 */
	boolean abort_mismatch() default true;

	public static class TypeChecker {
		@SuppressWarnings({ "unchecked" })
		public static <_Tp> boolean check(Class<_Tp> targetClass, Class<?>... ctorTypes) {
			java_type.wrapper<Constructor<_Tp>> target = java_type.wrapper.wrap();
			java_type.wrapper<CodecTarget> info = java_type.wrapper.wrap();
			reflection.class_operation.walk_constructors(targetClass, CodecTarget.class, (Constructor<_Tp> c, CodecTarget annotation) -> {
				if (target.value == null) {
					target.value = c;
					info.value = annotation;
				} else {
					throw new IllegalStateException("Class " + targetClass.getName() + " has multiple CodecTarget annotated constructor, at most 1 is valid.");
				}
				return true;
			});
			if (target.value == null)// 若目标类没有标记CodecTarget的构造函数，则通过检查
				return true;
			CodecTarget anno = info.value;
			boolean eq = Arrays.equals(ctorTypes, target.value.getParameterTypes());
			eq = anno.instant_check() ? eq : true;// 如果未启用instant_check，则始终可以通过该类型检查方法
			if (!eq && anno.abort_mismatch())
				throw new IllegalStateException("CODEC constructor args' types mismatch. Class " + targetClass.getName() + " should declare an empty constructor.");
			return eq;
		}
	}
}
