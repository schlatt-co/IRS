package io.github.jroy.irs;

import org.bukkit.plugin.java.JavaPlugin;

public class IRS extends JavaPlugin {

  @Override
  public void onEnable() {
    getLogger().info("Registering economy hook...");
    getServer().getPluginManager().registerEvents(new EconomyManager(), this);
  }
}
