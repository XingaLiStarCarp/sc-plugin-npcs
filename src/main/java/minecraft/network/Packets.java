package minecraft.network;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class Packets {

	/**
	 * 服务端向客户端发送实体动画packet
	 * 
	 * @param entity
	 * @param animation
	 * @param send
	 */
	public static final void broadcastClientboundAnimatePacket(Entity entity, int animation, boolean send) {
		Level level = entity.level();
		if (level instanceof ServerLevel serverLevel) {
			ClientboundAnimatePacket pkt = new ClientboundAnimatePacket(entity, animation);
			ServerChunkCache chunkCache = serverLevel.getChunkSource();
			if (send) {
				chunkCache.broadcastAndSend(entity, pkt);
			} else {
				chunkCache.broadcast(entity, pkt);
			}
		}
	}

	public static final void broadcastSwingAnimatePacket(Entity entity, InteractionHand hand) {
		broadcastClientboundAnimatePacket(entity, hand == InteractionHand.MAIN_HAND ? ClientboundAnimatePacket.SWING_MAIN_HAND : ClientboundAnimatePacket.SWING_OFF_HAND, false);
	}
}
