package ba.apt;

public class Main {
	public static String workspace_path = "D:\\JavaProjects\\bluearchive-extension\\src\\main\\";

	public static void main(String args[]) {
		// BaTexturePreprocessor.makeShaderColorTexture("resources\\assets\\ba\\textures\\sky\\environment\\rain.png", "resources\\assets\\ba\\textures\\sky\\environment\\\\raininv.png");
		tmp();
	}

	public static void tmp() {
		String texture_path = workspace_path + "resources\\assets\\ba\\textures\\item\\skillbooks";
		String class_output_path = workspace_path + "java\\ba\\entries\\item";
		// BaTexturePreprocessor.preprocess(texture_path, "Item_Icon_");

		ExtItemsClassFileGenerator.generateItemsClassFile(texture_path,
				"Skillbooks",
				class_output_path);

		texture_path = workspace_path + "resources\\assets\\ba\\textures\\item\\equipments";
		ExtItemsClassFileGenerator.generateItemsClassFile(texture_path,
				"Equipments",
				class_output_path);

		texture_path = workspace_path + "resources\\assets\\ba\\textures\\item\\materials";
		ExtItemsClassFileGenerator.generateItemsClassFile(texture_path,
				"Materials",
				class_output_path);

		// BaTexturePreprocessor.applyAlphaChannel("D:\\JavaProjects\\item_bg.png", "D:\\JavaProjects\\fR.png", "D:\\JavaProjects\\item_t1.png");
		// BaTexturePreprocessor.applyAlphaChannel("D:\\JavaProjects\\item_bg.png", "D:\\JavaProjects\\fSR.png", "D:\\JavaProjects\\item_t2.png");
		// BaTexturePreprocessor.applyAlphaChannel("D:\\JavaProjects\\item_bg.png", "D:\\JavaProjects\\fUR.png", "D:\\JavaProjects\\item_t3.png");
	}
}
