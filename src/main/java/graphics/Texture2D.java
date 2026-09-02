package graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL33;

import sys.jvm.file_system;

public class Texture2D {
	private int texture_id;

	public Texture2D(int width, int height) {
		texture_id = GL33.glGenTextures();
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture_id);
		GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA8, width, height, 0, GL33.GL_RGBA8, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
	}

	private void bufferPixels(BufferedImage img) {
		byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture_id);
		if (img.getAlphaRaster() != null)
			GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, 0, 0, img.getWidth(), img.getHeight(), GL33.GL_RGBA8, GL33.GL_UNSIGNED_BYTE, ByteBuffer.wrap(pixels));
		else
			GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, 0, 0, img.getWidth(), img.getHeight(), GL33.GL_RGB8, GL33.GL_UNSIGNED_BYTE, ByteBuffer.wrap(pixels));
	}

	public Texture2D(BufferedImage img) {
		this(img.getWidth(), img.getHeight());
		this.bufferPixels(img);
	}

	public static Texture2D read(String path) {
		Texture2D texture = null;
		try {
			texture = new Texture2D(ImageIO.read(new File(path)));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return texture;
	}

	public static Texture2D readInJar(Class<?> any_cls_in_jar, String path) {
		Texture2D texture = null;
		try {
			texture = new Texture2D(ImageIO.read(file_system.resource_stream(any_cls_in_jar, path)));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return texture;
	}

	public static Texture2D readInJar(String path) {
		Texture2D texture = null;
		try {
			texture = new Texture2D(ImageIO.read(file_system.resource_stream(path)));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return texture;
	}

	public void use(int texture_unit) {
		GL33.glActiveTexture(GL33.GL_TEXTURE0 + texture_unit);
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture_id);
	}

	public static int currentActiveTexture() {
		return GL33.glGetInteger(GL33.GL_ACTIVE_TEXTURE);
	}

	public static int currentBindTexture() {
		return GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
	}

	/**
	 * 查询指定的纹理宽度，不改变当前绑定纹理
	 * 
	 * @param texture
	 * @param mipmapLevel
	 * @return
	 */
	public static int textureWidth(int texture, int mipmapLevel) {
		int prev_texture = currentBindTexture();
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);
		int width = GL33.glGetTexLevelParameteri(GL33.GL_TEXTURE_2D, mipmapLevel, GL33.GL_TEXTURE_WIDTH);
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, prev_texture);
		return width;
	}

	public static int textureHeight(int texture, int mipmapLevel) {
		int prev_texture = currentBindTexture();
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);
		int height = GL33.glGetTexLevelParameteri(GL33.GL_TEXTURE_2D, mipmapLevel, GL33.GL_TEXTURE_HEIGHT);
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, prev_texture);
		return height;
	}

}
