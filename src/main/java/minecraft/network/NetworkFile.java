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
import java.util.function.Supplier;

import javabase.CheckCode;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.simple.SimpleChannel;
import scba.ModEntry;

public class NetworkFile {
	public static final String PROTOCOL_VERSION = "1.0";
	public static final String CHANNEL_ID = "network_file";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			ResourceLocation.fromNamespaceAndPath(ModEntry.MOD_ID, CHANNEL_ID),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	@FunctionalInterface
	public static interface FilePathResolver {
		public Path resolve(String fileName);
	}

	@FunctionalInterface
	public static interface ServerCheckOperation {
		/**
		 * @param checkSuccess 校验成功的文件列表
		 * @param checkFailed  校验失败的文件列表
		 */
		public void operate(Connection connection, List<String> checkSuccess, List<String> checkFailed);
	}

	/**
	 * 文件校验
	 * 服务端->客户端发送文件名和sha256校验码；
	 * 客户端->服务端发送文件名和是否校验成功。校验成功则对应的信息为""，校验失败则为客户端的实际sha256值；
	 */
	public static record FileCheckPacket(
			Map<String, String> fileCheckInfos) {

		public static void encode(FileCheckPacket packet, FriendlyByteBuf buf) {
			buf.writeInt(packet.fileCheckInfos.size());
			for (Map.Entry<String, String> entry : packet.fileCheckInfos.entrySet()) {
				buf.writeUtf(entry.getKey());
				buf.writeUtf(entry.getValue() == null ? "" : entry.getValue());
			}
		}

		public static FileCheckPacket decode(FriendlyByteBuf buf) {
			int size = buf.readInt();
			Map<String, String> checksums = new HashMap<>();
			for (int i = 0; i < size; i++) {
				String fileName = buf.readUtf();
				String checksum = buf.readUtf();
				checksums.put(fileName, checksum.isEmpty() ? null : checksum);
			}
			return new FileCheckPacket(checksums);
		}

		public static final int ID = 0;

		/**
		 * 客户端处理：比对校验码，返回需要比对结果给服务端
		 * 
		 * @param ctx
		 * @param clientFileResolver
		 */
		@OnlyIn(Dist.CLIENT)
		private void handleClient(NetworkEvent.Context ctx, FilePathResolver clientFileResolver) {
			ctx.enqueueWork(() -> {
				Map<String, String> checkResult = new HashMap<>();
				for (Map.Entry<String, String> entry : fileCheckInfos.entrySet()) {
					String fileName = entry.getKey();
					String serverCheckCode = entry.getValue();
					String localCheckCode = CheckCode.sha256(clientFileResolver.resolve(fileName));
					if (serverCheckCode.equals(localCheckCode)) {
						checkResult.put(fileName, "");
					} else {
						checkResult.put(fileName, localCheckCode);
					}
				}
				NetworkFile.CHANNEL.sendToServer(new FileCheckPacket(checkResult));// 将校验结果发送给服务端
			});
			ctx.setPacketHandled(true);
		}

		@OnlyIn(Dist.DEDICATED_SERVER)
		private static void handleServer(FileCheckPacket packet, Connection connection, ServerCheckOperation op) {
			ArrayList<String> checkSuccess = new ArrayList<>();
			ArrayList<String> checkFailed = new ArrayList<>();
			for (Entry<String, String> fileCheckResult : packet.fileCheckInfos().entrySet()) {
				if ("".equals(fileCheckResult.getValue())) {
					checkSuccess.add(fileCheckResult.getKey());
				} else {
					checkFailed.add(fileCheckResult.getKey());
				}
			}
			op.operate(connection, checkSuccess, checkFailed);
		}

		public static final void register(int id, FilePathResolver clientResolver, ServerCheckOperation serverOp) {
			NetworkFile.CHANNEL.registerMessage(
					id,
					FileCheckPacket.class,
					FileCheckPacket::encode,
					FileCheckPacket::decode,
					(packet, contextSupplier) -> {
						NetworkEvent.Context ctx = contextSupplier.get();
						if (FMLEnvironment.dist == Dist.CLIENT) {
							// 客户端处理
							packet.handleClient(ctx, clientResolver);
						} else {
							// 服务端处理
							ctx.enqueueWork(() -> {
								ServerPlayer player = ctx.getSender();
								if (player != null) {
									handleServer(packet, player.connection.connection, serverOp);
								}
							});
							ctx.setPacketHandled(true);
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
			byte[] chunkData) {

		public static void encode(FileDataPacket packet, FriendlyByteBuf buf) {
			buf.writeUtf(packet.fileName);
			buf.writeInt(packet.chunkIdx);
			buf.writeInt(packet.chunkNum);
			buf.writeInt(packet.chunkData.length);
			buf.writeBytes(packet.chunkData);
		}

		public static FileDataPacket decode(FriendlyByteBuf buf) {
			String fileName = buf.readUtf();
			int chunkIdx = buf.readInt();
			int chunkNum = buf.readInt();
			int dataLen = buf.readInt();
			byte[] chunkData = buf.readBytes(dataLen).array();
			return new FileDataPacket(fileName, chunkIdx, chunkNum, chunkData);
		}

		private static final Map<String, Map<Integer, byte[]>> chunksData = new HashMap<>();
		private static final Map<String, Integer> chunksNum = new HashMap<>();

		public static void handle(FileDataPacket packet, FileDataOperation op) {
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
				op.operate(fileName, dataArr);
				chunksData.remove(fileName);
				chunksNum.remove(fileName);
			}
		}

		public static final void register(int id, FileDataOperation op) {
			// 注册文件数据分块数据包
			NetworkFile.CHANNEL.registerMessage(
					id,
					FileDataPacket.class,
					FileDataPacket::encode,
					FileDataPacket::decode,
					(packet, contextSupplier) -> {
						Context ctx = contextSupplier.get();
						ctx.enqueueWork(() -> handle(packet, op));
						ctx.setPacketHandled(true);
					});
		}
	}

	/**
	 * 将本地文件分块读取。<br>
	 * 用于发送网络文件。
	 * 
	 * @param path
	 * @param chunkSize
	 * @return
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
					chunkIdx++;
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
	 * 
	 * @param clientFileResolver
	 * @param serverFileResolver
	 * @param chunkSize
	 */
	public static final void syncFiles(int checkId, int dataId, FilePathResolver clientFileResolver, FilePathResolver serverFileResolver, int chunkSize) {
		ServerCheckOperation syncFilesOp = (Connection connection, List<String> checkSuccess, List<String> checkFailed) -> {
			for (String fileName : checkFailed) {
				Path syncFile = serverFileResolver.resolve(fileName);
				byte[][] fileBytes = read(syncFile, chunkSize);// 分块读取文件
				for (int i = 0; i < fileBytes.length; ++i) {
					FileDataPacket dataPacket = new FileDataPacket(fileName, i, fileBytes.length, fileBytes[i]);
					CHANNEL.sendTo(dataPacket, connection, NetworkDirection.PLAY_TO_CLIENT);// 分块发送给客户端
				}
			}
		};
		FileCheckPacket.register(checkId, clientFileResolver, syncFilesOp);
		FileDataOperation fileSaveOp = (String fileName, byte[][] chunkBytes) -> {
			Path destFile = clientFileResolver.resolve(fileName);
			try (FileChannel channel = FileChannel.open(destFile)) {
				for (int chunkIdx = 0; chunkIdx < chunkBytes.length; ++chunkIdx) {
					channel.write(ByteBuffer.wrap(chunkBytes[chunkIdx]));// 将文件数据块全部按顺序写入文件
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		};
		FileDataPacket.register(dataId, fileSaveOp);
	}

	public static final void syncFiles(int checkId, int dataId, FilePathResolver clientFileResolver, FilePathResolver serverFileResolver) {
		syncFiles(checkId, dataId, clientFileResolver, serverFileResolver, DEFAULT_CHUNK_SIZE);
	}

	public static final void syncFiles(int checkId, int dataId, FilePathResolver fileResolver) {
		syncFiles(checkId, dataId, fileResolver, fileResolver);
	}
}
