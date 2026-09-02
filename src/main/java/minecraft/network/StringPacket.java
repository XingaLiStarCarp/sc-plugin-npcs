package minecraft.network;

import java.util.function.BiConsumer;

import io.netty.buffer.ByteBuf;
import minecraft.core.Core;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 字符串发包
 */
public class StringPacket implements CustomPacketPayload {
	public final int contentLength;
	public final String content;

	public StringPacket(String content) {
		this(content.length(), content);
	}

	private StringPacket(int contentLength, String content) {
		this.contentLength = contentLength;
		this.content = content;
	}

	public int getContentLength() {
		return contentLength;
	}

	public String getContent() {
		return content;
	}

	public static final Type<StringPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(Core.ModId, "string"));

	public static final StreamCodec<ByteBuf, StringPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			StringPacket::getContentLength,
			ByteBufCodecs.STRING_UTF8,
			StringPacket::getContent,
			StringPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	// 网络通道

	public static final String PROTOCOL_VERSION = "1.0";
	public static final String CHANNEL_ID = "string";

	private static PayloadRegistrar CHANNEL;
	private static boolean initialized = false;

	public static void register(PayloadRegistrar registrar) {
		CHANNEL = registrar;
		initialized = true;
	}

	private static void ensureInitialized() {
		if (!initialized) {
			throw new IllegalStateException("StringPacket not registered! Call StringPacket.register() in RegisterPayloadHandlersEvent.");
		}
	}

	/**
	 * 注册服务端字符串处理器（客户端->服务端）
	 * 
	 * @param handler 处理字符串的BiConsumer
	 */
	public static final void registerServerHandler(BiConsumer<String, IPayloadContext> handler) {
		if (CHANNEL == null) {
			Core.logError("CHANNEL is null, cannot register StringPacket server handler");
			return;
		}

		// playToServer: 客户端->服务端
		CHANNEL.playToServer(
				StringPacket.TYPE,
				StringPacket.STREAM_CODEC,
				(packet, ctx) -> {
					if (handler != null) {
						handler.accept(packet.content, ctx);
					}
				});
	}

	/**
	 * 注册客户端字符串处理器（服务端->客户端）
	 * 
	 * @param handler 处理字符串的BiConsumer
	 */
	public static final void registerClientHandler(BiConsumer<String, IPayloadContext> handler) {
		if (CHANNEL == null) {
			Core.logError("CHANNEL is null, cannot register StringPacket client handler");
			return;
		}

		// playToClient: 服务端->客户端
		CHANNEL.playToClient(
				StringPacket.TYPE,
				StringPacket.STREAM_CODEC,
				(packet, ctx) -> {
					if (handler != null) {
						handler.accept(packet.content, ctx);
					}
				});
	}

	/**
	 * 发送字符串数据包到服务端（客户端调用）
	 * 
	 * @param content 要发送的字符串内容
	 */
	public static final void sendToServer(String content) {
		ensureInitialized();
		PacketDistributor.sendToServer(new StringPacket(content));
	}

	/**
	 * 发送字符串数据包到指定客户端（服务端调用）
	 * 
	 * @param player  目标玩家
	 * @param content 要发送的字符串内容
	 */
	public static final void sendToPlayer(ServerPlayer player, String content) {
		ensureInitialized();
		PacketDistributor.sendToPlayer(player, new StringPacket(content));
	}
}