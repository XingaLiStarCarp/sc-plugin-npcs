package minecraft.level;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class Levels {
	public static void playSound(ServerPlayer player, SoundEvent sound, SoundSource source, double x, double y, double z, float volume, float pitch) {
		player.connection.send(new ClientboundSoundPacket(new Holder.Direct<>(sound), source, x, y, z, volume, pitch, player.level().getRandom().nextLong()));
	}
}
