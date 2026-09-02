package minecraft.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class DynamicTextures {
	/**
	 * 加载本地文件系统的贴图为动态纹理，并与指定的ResourceLocation关联
	 * 
	 * @param localPath     本地文件路径
	 * @param associatedLoc 关联的ResourceLocation
	 * @return 加载好的纹理的ResourceLocation。如果加载失败则返回null
	 */
	public static ResourceLocation load(String localPath, ResourceLocation associatedLoc) {
		Path path = Paths.get(localPath);
		if (Files.exists(path) && Files.isRegularFile(path)) {
			try (FileChannel channel = FileChannel.open(path)) {
				ByteBuffer buf = ByteBuffer.allocate((int) Files.size(path));
				channel.read(buf);
				DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(buf));
				Minecraft.getInstance().getTextureManager().register(associatedLoc, dynamicTexture);
				return associatedLoc;
			} catch (IOException ex) {
				return null;
			}
		} else {
			return null;
		}
	}

	public static ResourceLocation clientLoad(String localPath, ResourceLocation associatedLoc) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			return load(localPath, associatedLoc);
		} else {
			return associatedLoc;// 运行在服务端时则直接返回成功
		}
	}
}
