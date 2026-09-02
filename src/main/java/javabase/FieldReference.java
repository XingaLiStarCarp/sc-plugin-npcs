package javabase;

import java.lang.reflect.Field;

import sys.jvm.unsafe;
import sys.jvm.reflection;

/**
 * 用于重定向字段引用的封装工具类
 */
public class FieldReference implements Recoverable<FieldReference> {
	private Object obj;
	private Field field;
	private Object orig;
	private Object dest;

	private FieldReference(Object obj, String fieldName, Object dest) {
		this.obj = obj;
		this.field = reflection.find_declared_field(obj, fieldName);
		this.dest = dest;
		this.asPrimary();
	}

	private FieldReference(Object obj, String fieldName) {
		this(obj, fieldName, null);
	}

	/**
	 * 将当前值设置为原先值，可提供recovery()恢复到该值
	 * 
	 * @return
	 */
	public FieldReference asPrimary() {
		orig = unsafe.read_reference(obj, field);
		return this;
	}

	public FieldReference redirect(Object redirectRefValue) {
		unsafe.write(obj, field, redirectRefValue);
		return this;
	}

	public FieldReference redirect() {
		return redirect(dest);
	}

	/**
	 * 恢复到最开始的值
	 * 
	 * @return
	 */
	public final FieldReference recovery() {
		return redirect(orig);
	}

	public final FieldReference asPrimary(Object primaryValue) {
		this.orig = primaryValue;
		return this;
	}

	public final FieldReference redirectTo(Object redirectRefValue) {
		this.dest = redirectRefValue;
		return this;
	}

	public final Object realtimeValue() {
		return unsafe.read_reference(obj, field);
	}

	public static final FieldReference of(Object refObjBase, String refName, Object redirectRefValue) {
		if (refObjBase != null && refName != null)
			return new FieldReference(refObjBase, refName, redirectRefValue);
		return null;
	}

	public static final FieldReference of(Object refObjBase, String refName) {
		return of(refObjBase, refName, null);
	}
}
