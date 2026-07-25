package minecraft.network;

import java.util.function.BiConsumer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import scba.ModEntry;

/**
 * 字符串发包
 */
public class StringPacket {
	public final int contentLength;
	public final String content;

	public StringPacket(String content) {
		this(content.length(), content);
	}

	private StringPacket(int contentLength, String content) {
		this.contentLength = contentLength;
		this.content = content;
	}

	public static void encode(StringPacket packet, FriendlyByteBuf buf) {
		buf.writeInt(packet.contentLength);
		buf.writeUtf(packet.content, packet.contentLength);
	}

	public static StringPacket decode(FriendlyByteBuf buf) {
		int contentLength = buf.readInt();
		return new StringPacket(contentLength, buf.readUtf(contentLength));
	}

	// 网络通道

	public static final String PROTOCOL_VERSION = "1.0";
	public static final String CHANNEL_ID = "string";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			ResourceLocation.fromNamespaceAndPath(ModEntry.MOD_ID, CHANNEL_ID),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	/**
	 * 注册指定ID的字符串处理回调函数
	 * 
	 * @param id
	 * @param handler
	 */
	public static final void register(int id, BiConsumer<String, NetworkEvent.Context> handler) {
		StringPacket.CHANNEL.registerMessage(
				id,
				StringPacket.class,
				StringPacket::encode,
				StringPacket::decode,
				(packet, contextSupplier) -> {
					NetworkEvent.Context ctx = contextSupplier.get();
					if (handler != null) {
						handler.accept(packet.content, ctx);
					}
					ctx.setPacketHandled(true);
				});
	}
}