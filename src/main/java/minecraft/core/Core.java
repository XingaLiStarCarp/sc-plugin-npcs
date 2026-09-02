package minecraft.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import ba.ModEntry;
import minecraft.codec.annotation.CodecAutogen;
import minecraft.core.registry.RegistryFactory;
import minecraft.datagen.ExtDataGenerator;
import minecraft.datagen.annotation.RegistryEntry;
import minecraft.event.ClientLifecycleTrigger;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.ModLifecycleEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import sys.jvm.class_loader;
import sys.jvm.file_system;
import sys.jvm.reflection;
import sys.jvm.stack;
import sys.jvm.stack.skip_unwind;
import sys.jvm.unsafe;

@EventBusSubscriber
public class Core {
	public static final ModContainer Mod = null;
	public static final IEventBus ModBus = null;
	public static final Dist Env = FMLLoader.getDist();
	public static final String ModId = ModEntry.ModId;

	public static final String ModIdPrefix = ModId + ":";

	public static final Logger Logger = LogUtils.getLogger();

	private static Field ModLifecycleEvent_container;

	static {
		ModLifecycleEvent_container = reflection.find_declared_field(ModLifecycleEvent.class, "container");
	}

	/**
	 * 获取指定事件包装的ModContainer
	 * 
	 * @param event
	 * @return
	 */
	public static final ModContainer getModContainer(ModLifecycleEvent event) {
		return (ModContainer) unsafe.read_reference(event, ModLifecycleEvent_container);
	}

	/**
	 * 获取mod的事件总线
	 * 
	 * @param c mod容器
	 * @return
	 */
	public static final IEventBus getModEventBus(ModContainer c) {
		if (c instanceof FMLModContainer fmlc)
			return fmlc.getEventBus();
		else
			throw new RuntimeException("CANNOT get mod event bus in container " + c);
	}

	/**
	 * 获取mod的事件总线
	 * 
	 * @param event 事件
	 * @return
	 */
	public static final IEventBus getModEventBus(ModLifecycleEvent event) {
		return getModEventBus(getModContainer(event));
	}

	private static final ArrayList<String> preloadLibs = new ArrayList<>();

	private static boolean loadedPreloadLibs = false;

	/**
	 * 添加预加载的库
	 * 
	 * @param libs
	 */
	public static final void preloadLibs(String... libs) {
		if (loadedPreloadLibs)
			throw new IllegalStateException("Preload stage has passed, call this method in mod entry class's <cinit> or <init>.");
		else
			preloadLibs.addAll(List.of(libs));
	}

	private static void loadLibrary() {
		for (String lib : preloadLibs) {
			Core.logInfo("Preloading library " + lib);
			class_loader.load(true, lib); // FML的实际类加载器是fallbackClassLoader
		}
		loadedPreloadLibs = true;
	}

	/**
	 * Mod构造函数调用后，ModInit注解方法执行前调用
	 * 
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	private static final void preinit(FMLConstructModEvent event) {
		loadLibrary();
		unsafe.write_static(Core.class, "Mod", getModContainer(event));
		unsafe.write_static(Core.class, "ModBus", getModEventBus(Mod));// 初始化赋值ModBus
		load(true, "minecraft.core.registry.registries");// 加载并初始化注册表的字段初始化器
		load(true, "minecraft.terrain.algorithm");// 加载地形生成算法包以生成对应的CODEC
		ClientLifecycleTrigger.CLIENT_CONNECT.addCallback(EventPriority.HIGHEST, (ClientLevel level, RegistryAccess.Frozen registryAccess) -> {
			ModInit.Initializer.executeAllInitFuncs(null, ModInit.Stage.CLIENT_CONNECT);
		});
	}

	/**
	 * ModInit注解方法执行后调用
	 * 
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	private static final void postinit(NewRegistryEvent event) {
		ModInit.Initializer.executeAllInitFuncs(event, ModInit.Stage.PRE_INIT);
		CodecAutogen.CodecGenerator.generateCodecs();// 生成CODEC
		RegistryEntry.DeferredEntryHolderRegister.registerAll();
		RegistryFactory.registerAll();// 注册DeferredRegister所有新添加的注册表及其条目，必须在Codec注册之后调用，否则Codec不会被注册
		ModInit.Initializer.executeAllInitFuncs(event, ModInit.Stage.POST_INIT);
	}

	/**
	 * 数据生成，在注册完各种条目和codec之后才执行。
	 * 使用@ModInit注解在PRE_INIT阶段完成load()的类加载，之后将会依次生成CODEC、注册条目、datagen。
	 * 
	 * @param event
	 */
	@SubscribeEvent
	public static void datagen(GatherDataEvent event) {
		ExtDataGenerator.datagen(event);
	}

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

	@skip_unwind
	public static final String classpath(Class<?> clazz) {
		return file_system.classpath(clazz, FML_CLASSPATH_RESOLVER);
	}

	@skip_unwind
	public static final String classpath() {
		return classpath(stack.get_caller_class());
	}

	/**
	 * 双端加载指定包名的全部类
	 * 
	 * @param init
	 * @param start_path
	 * @param include_subpackage
	 */
	@skip_unwind
	public static final void load(boolean init, String start_path, boolean include_subpackage) {
		class_loader.load(FML_CLASSPATH_RESOLVER, init, start_path, include_subpackage);
		Logger.info("Loaded package " + start_path);
	}

	@skip_unwind
	public static final void load(boolean init, String start_path) {
		load(init, start_path, true);
	}

	/**
	 * 仅客户端端加载指定包名的全部类
	 * 
	 * @param init
	 * @param start_path
	 * @param include_subpackage
	 */
	@skip_unwind
	public static void loadClient(boolean init, String start_path, boolean include_subpackage) {
		ExecuteIn.Client(() -> {
			class_loader.load(FML_CLASSPATH_RESOLVER, init, start_path, include_subpackage);
			Logger.info("Loaded client-side package " + start_path);
		});
	}

	@skip_unwind
	public static final void loadClient(boolean init, String start_path) {
		loadClient(init, start_path, true);
	}

	private static boolean printLog = true;

	public static final void setPrintLog(boolean print) {
		printLog = print;
	}

	public static final void logDebug(String msg, Object... args) {
		if (printLog)
			Logger.debug(msg, args);
	}

	public static final void logInfo(String msg, Object... args) {
		if (printLog)
			Logger.info(msg, args);
	}

	public static final void logWarn(String msg, Object... args) {
		if (printLog)
			Logger.warn(msg, args);
	}

	public static final void logError(String msg, Object... args) {
		if (printLog)
			Logger.error(msg, args);
	}

	public static final String throwableString(Throwable th) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		th.printStackTrace(pw);
		return sw.toString();
	}
}
