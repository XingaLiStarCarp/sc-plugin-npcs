package minecraft;

import java.nio.file.Path;
import java.util.function.Function;

import jvmsp.class_loader;
import jvmsp.file_system;
import net.minecraftforge.fml.loading.FMLPaths;

public class ModPaths {
	public static final Path config() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static final Path config(String relativePath) {
		return config().resolve(relativePath);
	}

	public static final Path gameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	public static final Path gameDir(String relativePath) {
		return gameDir().resolve(relativePath);
	}

	public static String BUILD_DIR = "build";

	public static String CLASSES_DIR = "classes/java/main";

	/**
	 * FML的Classpath路径处理，比起原本的路径末尾多类似"#xyz!"的路径
	 */
	public static final Function<String, String> FML_CLASSPATH_RESOLVER = (classpath) -> {
		classpath = classpath.substring(0, classpath.lastIndexOf('#'));
		if (classpath.endsWith(file_system.jar_extension_name)) {
			return classpath;// jar内运行
		} else {
			// 开发环境运行
			return classpath.substring(0, classpath.lastIndexOf("/" + BUILD_DIR + "/") + BUILD_DIR.length() + 2) + CLASSES_DIR;
		}
	};

	public static final String classpath(Class<?> clazz) {
		return file_system.classpath(clazz, FML_CLASSPATH_RESOLVER);
	}

	public static final void load(boolean init, String start_path, boolean include_subpackage) {
		class_loader.load(FML_CLASSPATH_RESOLVER, init, start_path, include_subpackage);
	}
}
