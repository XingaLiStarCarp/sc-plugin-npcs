package minecraft.core.registry;

import java.lang.reflect.Type;

import net.minecraft.core.Holder;
import sys.jvm.reflection;
import sys.jvm.unsafe;

public class Holders {
	public static final Class<?> getHolderType(Type holderField) {
		return reflection.first_generic_class(holderField);
	}

	public static final <T, H extends Holder<T>> H bindValue(H holder, T value) {
		unsafe.write_member(holder, "value", value);
		return holder;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T getValue(Holder<T> holder) {
		return (T) unsafe.read_member_reference(holder, "value");
	}
}
