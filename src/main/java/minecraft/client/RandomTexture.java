package minecraft.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import minecraft.ModPaths;
import net.minecraft.resources.ResourceLocation;

/**
 * 随机纹理
 */
public class RandomTexture {
	private ThreadLocalRandom randomSource;
	private ArrayList<ResourceLocation> textures;

	private RandomTexture() {
		randomSource = ThreadLocalRandom.current();
		textures = new ArrayList<>();
	}

	public RandomTexture add(ResourceLocation textureLoc) {
		textures.add(textureLoc);
		return this;
	}

	public RandomTexture remove(ResourceLocation textureLoc) {
		textures.remove(textureLoc);
		return this;
	}

	/**
	 * 随机获取一个纹理贴图
	 * 
	 * @return
	 */
	public ResourceLocation get() {
		return textures.get(randomSource.nextInt(textures.size()));
	}

	/**
	 * 加载本地文件或文件夹作为随机贴图源
	 * 
	 * @param namespace ResourceLocation命名空间
	 * @param locPrefix ResourceLocation的路径前缀
	 * @param localPath
	 * @return
	 */
	public static final RandomTexture local(String namespace, String locPrefix, Path localPath) {
		if (Files.exists(localPath)) {
			final RandomTexture rt = new RandomTexture();
			if (Files.isRegularFile(localPath)) {
				ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(namespace, locPrefix + localPath.getFileName());
				rt.add(DynamicTextures.clientLoad(localPath.toString(), loc));
			} else {
				try (Stream<Path> files = Files.walk(localPath)) {
					files.filter(Files::isRegularFile)
							.forEach((f) -> {
								ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(namespace, locPrefix + f.getFileName());
								rt.add(DynamicTextures.clientLoad(f.toString(), loc));
							});
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return rt;
		} else {
			return null;
		}
	}

	public static final RandomTexture local(String namespace, String locPrefix, String localPath) {
		return local(namespace, locPrefix, Paths.get(localPath));
	}

	public static final RandomTexture of(Collection<ResourceLocation> textureLocs) {
		RandomTexture rt = new RandomTexture();
		rt.textures.addAll(textureLocs);
		return rt;
	}

	public static final RandomTexture of(ResourceLocation... textureLocs) {
		return of(List.of(textureLocs));
	}

	/**
	 * 从config目录下读取子目录下的纹理
	 * 
	 * @param namespace
	 * @param locPrefix
	 * @param configRelativeDir
	 * @return
	 */
	public static final RandomTexture localConfig(String namespace, String locPrefix, String configRelativeDir) {
		return local(namespace, locPrefix, ModPaths.config(configRelativeDir));
	}
}
