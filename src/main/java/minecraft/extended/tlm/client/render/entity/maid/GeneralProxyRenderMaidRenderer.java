package minecraft.extended.tlm.client.render.entity.maid;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntityRenderer;

import jvmsp.unsafe;
import minecraft.client.graphics.render.entity.ProxyRenderEntityRenderer;
import minecraft.extended.tlm.entity.maid.ProxyRenderMaid;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;

/**
 * Touhou Little Maid模组的实体渲染。<br>
 */
public abstract class GeneralProxyRenderMaidRenderer extends ProxyRenderEntityRenderer<ProxyRenderMaid, EntityMaidRenderer> {
	protected final IGeoEntityRenderer<Mob> ysmMaidRenderer;

	@SuppressWarnings("unchecked")
	public GeneralProxyRenderMaidRenderer(EntityRendererProvider.Context context) {
		super(context, new EntityMaidRenderer(context));
		ysmMaidRenderer = (IGeoEntityRenderer<Mob>) unsafe.read_member_reference(this.proxyEntityRenderer, "ysmMaidRenderer");
	}

	/**
	 * 获取YSM渲染实体。<br>
	 * 调用此方法后YSM渲染实体已经实际存入了EntityMaid对象的Capability，YSM在渲染时也是直接从Capability中获取，因此本方法返回值可以丢弃。<br>
	 * 
	 * @param maidEntity
	 * @return
	 */
	public IGeoEntity getOrCreateYsmGeoEntityCapability(EntityMaid maidEntity) {
		// ysmMaidRenderer.getGeoEntity()使用强制转换将参数转换为EntityMaid，如果仅仅是实现了IMaid接口将转换失败导致崩溃
		// 该函数将使用getCapability()获取实体的YSM模型的IGeoEntity，若已存在则返回现存值，否则创建新值。
		// YSM渲染依赖entity.getCapability()，如果获取到的IGeoEntity Capability为empty，则不执行渲染。因此CapabilityProvider必须正确初始化。
		IGeoEntity geoEntity = this.ysmMaidRenderer.getGeoEntity(maidEntity);
		geoEntity.setYsmModel(maidEntity.getYsmModelId(), maidEntity.getYsmModelTexture());
		geoEntity.updateRoamingVars(maidEntity.roamingVars);
		return geoEntity;
	}
}
