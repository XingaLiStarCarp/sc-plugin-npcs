package minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public enum LogicalEnd {
	/**
	 * 加入多人游戏的客户端
	 */
	PURE_CLIENT(true),
	/**
	 * 多人游戏的服务端
	 */
	PURE_SERVER(false),
	/**
	 * 本地游戏，既有客户端又有本地服务端
	 */
	LOCAL_SERVER(true);

	private final boolean isClientAvailable;

	private LogicalEnd(boolean isClientAvailable) {
		this.isClientAvailable = isClientAvailable;
	}

	/**
	 * 当前是否可用客户端的类
	 * 
	 * @return
	 */
	public final boolean isClientAvailable() {
		return this.isClientAvailable;
	}

	public static final boolean isClient() {
		return FMLEnvironment.dist == Dist.CLIENT;
	}

	public static final Level clientLevel() {
		if (isClient()) {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null)
				return mc.level;
		}
		return null;
	}

	/**
	 * 判断当前运行环境
	 * 
	 * @return
	 */
	public static LogicalEnd host() {
		if (isClient()) {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null) {
				if (mc.isLocalServer())
					return LOCAL_SERVER;
				else
					return PURE_CLIENT;
			}
		}
		return PURE_SERVER;
	}

	public static boolean isHostPureClient() {
		return host() == PURE_CLIENT;
	}

	public static boolean isHostPureServer() {
		return host() == PURE_SERVER;
	}

	public static boolean isHostLocalServer() {
		return host() == LOCAL_SERVER;
	}
}
