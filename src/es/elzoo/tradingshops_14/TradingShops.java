package es.elzoo.tradingshops_14;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import es.elzoo.tradingshops_14.gui.GUIEventos;
import net.milkbowl.vault.economy.Economy;

public class TradingShops extends JavaPlugin {
	private File configFile;
	public static FileConfiguration config;
	public static WorldGuardLoader wgLoader = null;
	
	private static String cadenaConex;
	private static Economy economy = null;
	private static Connection conexion = null;
	
	private TradingShops ts;
	
	public TradingShops(TradingShops ts) {
		this.ts = ts;
	}
	
	@Override
	public void onLoad() {		
		Plugin wgCheck = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if(wgCheck != null) {
			wgLoader = new WorldGuardLoader();
		}			
	}
	
	@Override
	public void onEnable() {
		cadenaConex = "jdbc:sqlite:"+ts.getDataFolder().getAbsolutePath()+"/shops.db";
		
		this.setupEconomy();		
		this.crearConfig();
		
		ts.getServer().getPluginManager().registerEvents(new EventosTienda(), ts);
		ts.getServer().getPluginManager().registerEvents(new GUIEventos(), ts);
		ts.getCommand("tradeshop").setExecutor(new ComandoTienda());
		
		try {
			conexion = DriverManager.getConnection(cadenaConex);
			this.crearTablas();
			Tienda.cargarDatos();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(ts, () -> {
			Tienda.tickTiendas();
		}, 40, 40);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(ts, () -> {
			Bukkit.getScheduler().runTaskAsynchronously(ts, () -> {
				Tienda.guardarDatos();
			});
		}, 6000, 6000);
	}	
	
	@Override
	public void onDisable() {
		Tienda.guardarDatos();
		
		if(conexion != null) {
			try {
				conexion.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void crearTablas() {
		PreparedStatement[] stmts = new PreparedStatement[] {};
		try {
			stmts = new PreparedStatement[] {
				conexion.prepareStatement("CREATE TABLE IF NOT EXISTS zooMercaTiendas(id INTEGER PRIMARY KEY autoincrement, location varchar(64), owner varchar(64));"),
				conexion.prepareStatement("CREATE TABLE IF NOT EXISTS zooMercaTiendasFilas(itemIn text, itemOut text, idTienda INTEGER);"),
				conexion.prepareStatement("CREATE TABLE IF NOT EXISTS zooMercaStocks(owner varchar(64), items JSON);")
			};
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		for(PreparedStatement stmt : stmts) {
			try {
				stmt.execute();
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}			
		}
		
		List<PreparedStatement> stmtsParches = new ArrayList<PreparedStatement>();
		try {
			stmtsParches.add(conexion.prepareStatement("ALTER TABLE zooMercaTiendasFilas ADD COLUMN broadcast BOOLEAN DEFAULT 0"));
			stmtsParches.add(conexion.prepareStatement("ALTER TABLE zooMercaStocks ADD COLUMN pag INTEGER DEFAULT 0"));
			stmtsParches.add(conexion.prepareStatement("ALTER TABLE zooMercaTiendas ADD COLUMN admin BOOLEAN DEFAULT FALSE;"));			
		} catch(Exception e) {
			
		}
		
		for(int i=0,len=stmtsParches.size(); i<len; i++) {
			try {
				stmtsParches.get(i).execute();
				stmtsParches.get(i).close();
			} catch(Exception e) {
				
			}
		}
	}
	
	public static Connection getConexion() {
		checkConexion();
		return conexion;
	}
	
	public static void checkConexion() {
		try {
			if(conexion == null || conexion.isClosed() || !conexion.isValid(0)) {
				conexion = DriverManager.getConnection(cadenaConex);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean setupEconomy() {
		if(ts.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
		
		RegisteredServiceProvider<Economy> rsp = ts.getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) {
			return false;
		}
		
		economy = rsp.getProvider();
		return economy != null;
	}
	
	public static Optional<Economy> getEconomy() {
		return Optional.ofNullable(economy);
	}
	
	public void crearConfig() {
		this.configFile = new File(ts.getDataFolder(), "config.yml");
		if(!this.configFile.exists()) {
			this.configFile.getParentFile().mkdirs();
			ts.saveResource("config.yml", false);
		}
		
		config = new YamlConfiguration();
		
		try {
			config.load(this.configFile);
			
			String ver = config.getDouble("configVersion")+"";			
			if(ver.equals("1.0")) {
				config.set("deleteBlock", false);
				config.set("showParticles", true);
				config.set("configVersion", 1.1);
			} else if(ver.equals("1.1")) {
				config.set("enableWorldGuardFlag", true);
			} else if(ver.equals("1.2")) {
				config.set("stockPages", 5);
			} else if(ver.equals("1.3")) {
				config.set("usePermissions", true);
				config.set("defaultShopLimit", 5);
			} else if(ver.equals("1.4")) {
				config.set("noPermissions", "&cYou do not have permissions to do that.");
				config.set("deleteShop", "&7Click on the shop you want to delete.");
				config.set("createShop", "&7Click the block you want to create the shop on.");
				config.set("shopNotOwned", "&cYou do not own this shop!" );
				config.set("shopDeleted", "&7Shop deleted.");
				config.set("shopCreated", "&7Shop created.");
				config.set("shopLimit", "&cYou have reached your shops amount limit.");
				config.set("noItems", "&cYou do not have enough items.");
				config.set("noStock", "&cThe shop does not have enough stock.");
				config.set("noStockNotify", "&cOne of your shops has run out of stock! &o(%s)");
				config.set("buy", "&7You bought&7 %in &7for&c %out&7.");
				config.set("sell", "&7%p has bought&7 %in &7for&c %out&7.");
				config.set("noMoney", "&cYou do not have enough money to create a shop. You need at least &o$");
				config.set("reload", "&7Configuration file reloaded.");
			}
		} catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
