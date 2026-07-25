package minecraft.capability;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;

import jvmsp.reflection;
import jvmsp.reflection.reflection_factory;
import jvmsp.symbols;
import jvmsp.unsafe;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import scba.ModEntry;

@SuppressWarnings("unchecked")
public class CapabilityData {

	private static final long TOP_CapabilityProvider;
	private static final long BOTTOM_CapabilityProvider;

	static {
		TOP_CapabilityProvider = unsafe.top_offset(CapabilityProvider.class);
		BOTTOM_CapabilityProvider = unsafe.bottom_offset(CapabilityProvider.class);
	}

	private static Constructor<CapabilityProvider<?>> CapabilityProvider_ctor;
	private static MethodHandle CapabilityProvider_gatherCapabilities;

	static {
		CapabilityProvider_ctor = (Constructor<CapabilityProvider<?>>) reflection.find_constructor(CapabilityProvider.class, Class.class, boolean.class);
		CapabilityProvider_gatherCapabilities = symbols.find_special_method(CapabilityProvider.class, "gatherCapabilities", void.class);
	}

	public static final <_B extends CapabilityProvider<_B>, _T extends _B> _T construct(Class<_T> subClazz, final Class<_B> baseClazz, final boolean isLazy) {
		return reflection_factory.construct(subClazz, CapabilityProvider_ctor, baseClazz, isLazy);
	}

	public static final <_B extends CapabilityProvider<_B>, _T extends _B> _T construct(Class<_T> subClazz, final Class<_B> baseClazz) {
		return construct(subClazz, baseClazz, false);
	}

	/**
	 * 收集实体的Capability。实体的level通常情况下需要非null，否则收集时可能会有些Capability会访问level导致空指针
	 * 
	 * @param entity
	 */
	public static final void gatherCapabilities(CapabilityProvider<?> provider) {
		try {
			CapabilityProvider_gatherCapabilities.invoke(provider);// 必须执行CapabilityProvider.gatherCapabilities()，否则getCapability()方法将始终返回LazyOptional.empty()
		} catch (Throwable ex) {
			ModEntry.LOGGER.error("gather capabilities for '" + provider + "' failed", ex);
		}
	}

	/**
	 * 子类的字段可能与CapabilityProvider的字段有交叉导致覆写原本的CapabilityProvider。<br>
	 * 因此需要先拷贝原本的CapabilityProvider用于临时存放数据。<br>
	 * 
	 * @param srcEntity
	 * @param destEntity
	 */
	public static final <_T extends ICapabilityProviderImpl<_T>> CapabilityProvider<_T> copy(CapabilityProvider<_T> src) {
		CapabilityProvider<_T> cpy = unsafe.allocate(src.getClass());
		copy(src, cpy);
		return cpy;
	}

	public static final void copy(CapabilityProvider<?> src, CapabilityProvider<?> dest) {
		unsafe.copy_member_fields(src, dest, TOP_CapabilityProvider, BOTTOM_CapabilityProvider);
	}
}
