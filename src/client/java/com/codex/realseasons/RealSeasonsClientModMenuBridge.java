package com.codex.realseasons;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Hooks the Real Serene Seasons config screen into Mod Menu when Cloth Config is present.
 */
public final class RealSeasonsClientModMenuBridge implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
                return RealSeasonsClientConfigScreenFactory.create(parent);
            }
            return parent;
        };
    }
}
