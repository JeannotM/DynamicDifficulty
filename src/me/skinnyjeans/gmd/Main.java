/**
 * Main handler for the Gameplay-Modulated-difficulty plugin.
 *
 * @version 1.9
 * @author SkinnyJeans
 */
package me.skinnyjeans.gmd;

import me.skinnyjeans.gmd.managers.MainManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private MainManager MAIN_MANAGER;

    @Override
	public void onEnable() {
        MAIN_MANAGER = new MainManager(this);
    }
}
