package com.xLSC;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class XLSC extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("XingliStarCarp成功加载\n        "+
                "*          *   *                    *********         ******      \n        "+
                " *        *    *                 ***                **      **    \n        "+
                "  *      *     *                *                  *          *   \n        "+
                "   *    *      *                *                 *               \n        "+
                "    *  *       *                 **              *                \n        "+
                "     *         *                    *****        *                \n        "+
                "    *  *       *                          **     *                \n        "+
                "   *    *      *                            *     *               \n        "+
                "  *      *     *                            *      *          *   \n        "+
                " *        *    *                         ***        **      **    \n        "+
                "*          *   *************   **********             ******      \n        ");
        Bukkit.getPluginManager().registerEvents(new ResidenceListener(), this);
        Bukkit.getPluginManager().registerEvents(new lp(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("XingliStarCarp成功卸载\n        "+
                "*          *   *                    *********         ******      \n        "+
                " *        *    *                 ***                **      **    \n        "+
                "  *      *     *                *                  *          *   \n        "+
                "   *    *      *                *                 *               \n        "+
                "    *  *       *                 **              *                \n        "+
                "     *         *                    *****        *                \n        "+
                "    *  *       *                          **     *                \n        "+
                "   *    *      *                            *     *               \n        "+
                "  *      *     *                            *      *          *   \n        "+
                " *        *    *                         ***        **      **    \n        "+
                "*          *   *************   **********             ******      \n        ");
    }
}
