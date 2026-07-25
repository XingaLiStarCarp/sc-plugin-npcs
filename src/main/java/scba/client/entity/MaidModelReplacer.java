package scba.client.entity;

import minecraft.extended.tlm.client.render.entity.maid.MaidModelDispatcher;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid.MaidModelAsset;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class MaidModelReplacer {
	@SubscribeEvent
	public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		MaidModelDispatcher.dispatch(EntityType.SLIME, (entity, model) -> {
			model.setTlmModelId(MaidModelAsset.hashTlmModelId(entity.getUUID()));
		});
		MaidModelDispatcher.dispatch(EntityType.WITHER, (entity, model) -> {
			model.setIsYsmModel(true);
			model.setYsmModelId("ba_白洲梓（泳装）.2.0.ysm");
		});
		MaidModelDispatcher.dispatch(EntityType.WARDEN, (entity, model) -> {
			model.setIsYsmModel(true);
			model.setYsmModelId("BA_空崎日奈：礼服.ysm");
		});
		MaidModelDispatcher.dispatch(EntityType.IRON_GOLEM, (entity, model) -> {
			model.setIsYsmModel(true);
			model.setYsmModelId(MaidModelAsset.hashLocalCustomYsmModelId(entity.getUUID()));
		});
	}
}
