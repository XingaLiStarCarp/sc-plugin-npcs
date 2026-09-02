package minecraft.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javabase.CheckCode;
import minecraft.core.Core;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkFile {
	public static final String PROTOCOL_VERSION = "1.0";
	public static final String CHANNEL_ID = "network_file";

	private static PayloadRegistrar CHANNEL;
	private static boolean initialized = false;

	public static void register(PayloadRegistrar registrar) {
		CHANNEL = registrar;
		FileCheckPacket.registerServer();
		FileDataPacket.registerServer();
		initialized = true;
	}

	private static void ensureInitialized() {
		if (!initialized) {
			throw new IllegalStateException("NetworkFile not registered! Call NetworkFile.register() in RegisterPayloadHandlersEvent.");
		}
	}

	@FunctionalInterface
	public static interface FilePathResolver {
		public Path resolve(String fileName);
	}

	@FunctionalInterface
	public static interface ServerCheckOperation {
		/**
		 * @param player       执行操作的服务器玩家
		 * @param checkSuccess 校验成功的文件列表
		 * @param checkFailed  校验失败的文件列表
		 */
		public void operate(ServerPlayer player, List<String> checkSuccess, List<String> checkFailed);
	}

	/**
	 * 文件校验
	 * 服务端->客户端发送文件名和sha256校验码；
	 * 客户端->服务端发送文件名和是否校验成功。校验成功则对应的信息为""，校验失败则为客户端的实际sha256值；
	 */
	public static record FileCheckPacket(Map<String, String> fileCheckInfos) implements CustomPacketPayload {

		public static final Type<FileCheckPacket> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(Core.ModId, "file_check"));

		public static final StreamCodec<RegistryFriendlyByteBuf, Map<String, String>> MAP_CODEC = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8);

		public static final StreamCodec<RegistryFriendlyByteBuf, FileCheckPacket> STREAM_CODEC = StreamCodec.composite(
				MAP_CODEC,
				FileCheckPacket::fileCheckInfos,
				FileCheckPacket::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}

		public static void registerServer() {
			if (CHANNEL == null) {
				Core.logError("CHANNEL is null, cannot register FileCheckPacket server handler");
				return;
			}
			// playToServer: 客户端->服务端
			CHANNEL.playToServer(
					FileCheckPacket.TYPE,
					FileCheckPacket.STREAM_CODEC,
					FileCheckPacket::handleServer);
		}

		private static FilePathResolver clientResolver;
		private static ServerCheckOperation serverOp;

		public static void setClientResolver(FilePathResolver resolver) {
			clientResolver = resolver;
		}

		public static void setServerOperation(ServerCheckOperation op) {
			serverOp = op;
		}

		/**
		 * 客户端处理：比对校验码，返回需要比对结果给服务端
		 */
		@OnlyIn(Dist.CLIENT)
		public static void handleClient(FileCheckPacket packet, IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				if (clientResolver == null) {
					Core.logError("ClientFileResolver not set for FileCheckPacket");
					return;
				}
				Map<String, String> checkResult = new HashMap<>();
				for (Map.Entry<String, String> entry : packet.fileCheckInfos.entrySet()) {
					String fileName = entry.getKey();
					String serverCheckCode = entry.getValue();
					String localCheckCode = CheckCode.sha256(clientResolver.resolve(fileName));
					if (serverCheckCode.equals(localCheckCode)) {
						checkResult.put(fileName, "");
					} else {
						checkResult.put(fileName, localCheckCode);
					}
				}
				PacketDistributor.sendToServer(new FileCheckPacket(checkResult));
			});
		}

		/**
		 * 服务端处理：处理客户端返回的校验结果
		 */
		public static void handleServer(FileCheckPacket packet, IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				if (serverOp == null) {
					Core.logError("ServerCheckOperation not set for FileCheckPacket");
					return;
				}
				ServerPlayer player = (ServerPlayer) ctx.player();
				if (player != null) {
					ArrayList<String> checkSuccess = new ArrayList<>();
					ArrayList<String> checkFailed = new ArrayList<>();
					for (Entry<String, String> fileCheckResult : packet.fileCheckInfos().entrySet()) {
						if ("".equals(fileCheckResult.getValue())) {
							checkSuccess.add(fileCheckResult.getKey());
						} else {
							checkFailed.add(fileCheckResult.getKey());
						}
					}
					// 直接传入 player
					serverOp.operate(player, checkSuccess, checkFailed);
				}
			});
		}
	}

	@FunctionalInterface
	public static interface FileDataOperation {
		public void operate(String fileName, byte[][] chunkBytes);
	}

	public static record FileDataPacket(
			String fileName,
			int chunkIdx,
			int chunkNum,
			byte[] chunkData) implements CustomPacketPayload {

		public static final Type<FileDataPacket> TYPE = new Type<>(
				ResourceLocation.fromNamespaceAndPath(Core.ModId, "file_data"));

		public static final StreamCodec<RegistryFriendlyByteBuf, FileDataPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8,
				FileDataPacket::fileName,
				ByteBufCodecs.INT,
				FileDataPacket::chunkIdx,
				ByteBufCodecs.INT,
				FileDataPacket::chunkNum,
				ByteBufCodecs.BYTE_ARRAY,
				FileDataPacket::chunkData,
				FileDataPacket::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}

		public static void registerServer() {
			if (CHANNEL == null) {
				Core.logError("CHANNEL is null, cannot register FileDataPacket server handler");
				return;
			}
			CHANNEL.playToServer(
					FileDataPacket.TYPE,
					FileDataPacket.STREAM_CODEC,
					FileDataPacket::handleServer);
		}

		private static FileDataOperation dataOp;

		public static void setDataOperation(FileDataOperation op) {
			dataOp = op;
		}

		private static final Map<String, Map<Integer, byte[]>> chunksData = new HashMap<>();
		private static final Map<String, Integer> chunksNum = new HashMap<>();

		@OnlyIn(Dist.CLIENT)
		public static void handleClient(FileDataPacket packet, IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				if (dataOp == null) {
					Core.logError("FileDataOperation not set for FileDataPacket");
					return;
				}
				String fileName = packet.fileName();
				int chunkIdx = packet.chunkIdx();
				int chunkNum = packet.chunkNum();
				byte[] chunkData = packet.chunkData();

				chunksNum.put(fileName, chunkNum);
				Map<Integer, byte[]> chunks = chunksData.computeIfAbsent(fileName, f -> new HashMap<>());
				chunks.put(chunkIdx, chunkData);
				if (chunks.size() == chunkNum) {
					byte[][] dataArr = new byte[chunkNum][];
					for (int i = 0; i < chunkNum; i++) {
						dataArr[i] = chunks.get(i);
					}
					dataOp.operate(fileName, dataArr);
					chunksData.remove(fileName);
					chunksNum.remove(fileName);
				}
			});
		}

		public static void handleServer(FileDataPacket packet, IPayloadContext ctx) {
			// 服务端收到客户端发送的FileDataPacket（目前只有客户端接收文件，所以服务端不处理）
		}
	}

	/**
	 * 将本地文件分块读取
	 */
	public static final byte[][] read(Path path, int chunkSize) {
		if (Files.exists(path) && Files.isRegularFile(path)) {
			try (FileChannel channel = FileChannel.open(path)) {
				long fileSize = Files.size(path);
				int chunkNum = (int) Math.ceil((double) fileSize / chunkSize);
				byte[][] chunks = new byte[chunkNum][];
				int bytesRead;
				byte[] buf = new byte[chunkSize];
				for (int chunkIdx = 0; chunkIdx < chunkNum; ++chunkIdx) {
					bytesRead = channel.read(ByteBuffer.wrap(buf));
					byte[] chunkData = new byte[bytesRead];
					System.arraycopy(buf, 0, chunkData, 0, bytesRead);
					chunks[chunkIdx] = chunkData;
				}
				return chunks;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return null;
		} else {
			return null;
		}
	}

	public static final int DEFAULT_CHUNK_SIZE = 4096;

	/**
	 * 同步客户端文件
	 */
	public static final void syncFiles(FilePathResolver clientFileResolver, FilePathResolver serverFileResolver, int chunkSize) {
		ensureInitialized();

		FileCheckPacket.setClientResolver(clientFileResolver);

		FileDataPacket.setDataOperation((String fileName, byte[][] chunkBytes) -> {
			Path destFile = clientFileResolver.resolve(fileName);
			try (FileChannel channel = FileChannel.open(destFile)) {
				for (int chunkIdx = 0; chunkIdx < chunkBytes.length; ++chunkIdx) {
					channel.write(ByteBuffer.wrap(chunkBytes[chunkIdx]));
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});

		// 修改：ServerCheckOperation 接收 ServerPlayer 参数
		ServerCheckOperation syncFilesOp = (ServerPlayer player, List<String> checkSuccess, List<String> checkFailed) -> {
			for (String fileName : checkFailed) {
				Path syncFile = serverFileResolver.resolve(fileName);
				byte[][] fileBytes = read(syncFile, chunkSize);
				for (int i = 0; i < fileBytes.length; ++i) {
					FileDataPacket dataPacket = new FileDataPacket(fileName, i, fileBytes.length, fileBytes[i]);
					// 直接使用 ServerPlayer 发送数据包
					PacketDistributor.sendToPlayer(player, dataPacket);
				}
			}
		};
		FileCheckPacket.setServerOperation(syncFilesOp);
	}

	public static final void syncFiles(FilePathResolver clientFileResolver, FilePathResolver serverFileResolver) {
		syncFiles(clientFileResolver, serverFileResolver, DEFAULT_CHUNK_SIZE);
	}

	public static final void syncFiles(FilePathResolver fileResolver) {
		syncFiles(fileResolver, fileResolver);
	}
}