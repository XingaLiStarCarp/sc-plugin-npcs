package minecraft.entity.mob;

import minecraft.entity.Humanoid;
import minecraft.entity.ProxyRenderEntity;
import minecraft.entity.Humanoid.PlayerModelAsset;
import minecraft.entity.player.BlankPlayer;
import minecraft.entity.player.PlayerData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface ProxyRenderPlayer extends ProxyRenderEntity<Player, PlayerModelAsset>, Humanoid {

	/**
	 * 禁止覆写此方法。<br>
	 * 创建一个虚假的玩家渲染实体。<br>
	 */
	@Override
	public default Player blankRenderingEntity(Entity bindEntity) {
		return BlankPlayer.blankLocalPlayer();
	}

	@Override
	public default Player syncRenderingEntityData() {
		Player renderingEntity = ProxyRenderEntity.super.syncRenderingEntityData();
		Entity bindEntity = bindEntity();
		PlayerData.syncEntityData(bindEntity, renderingEntity);
		return renderingEntity;
	}

	public interface ProxyRenderPlayerEntity extends ProxyRenderPlayer, HumanoidEntity, ProxyRenderPlayerProvider {
		@Override
		public default SynchedEntityData entityData() {
			return ProxyRenderPlayer.super.entityData();
		}

		@Override
		public default ProxyRenderPlayer proxyRenderPlayer() {
			return this;
		}
	}

	public static interface ProxyRenderPlayerProvider {
		public abstract ProxyRenderPlayer proxyRenderPlayer();
	}
}
