package minecraft.entity.data;

import java.lang.reflect.Constructor;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import sys.jvm.reflection;
import sys.jvm.reflection.reflection_factory;

@SuppressWarnings("unchecked")
public class AttachmentHolderOp {
	private static Constructor<AttachmentHolder> AttachmentHolder_ctor;

	static {
		AttachmentHolder_ctor = (Constructor<AttachmentHolder>) reflection.find_constructor(AttachmentHolder.class);
	}

	public static final <_T extends AttachmentHolder> _T construct(Class<_T> holderClazz, boolean isLazy) {
		return reflection_factory.construct(holderClazz, AttachmentHolder_ctor);
	}
}
