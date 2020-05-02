package es.elzoo.tradingshops;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import es.elzoo.tradingshops.Mensajes;
import es.elzoo.tradingshops.inventarios.InvStock;

public class Tienda {
	private static Plugin plugin = Bukkit.getPluginManager().getPlugin("TradingShops");
	private static List<Tienda> tiendas = new ArrayList<Tienda>();
	
	private int idTienda;
	private UUID owner;
	private Location location;
	private FilaTienda[] filas;
	private boolean admin;
	
	private Tienda(int idTienda, UUID owner, Location loc, boolean admin) {
		this.idTienda = idTienda;
		this.owner = owner;
		this.location = loc;
		this.filas = new FilaTienda[4];
		this.admin = admin;
		
		if(idTienda == -1) {
			final Tienda tienda = this;
						
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				PreparedStatement stmt = null;
				try {
					stmt = TradingShops.getConexion().prepareStatement("INSERT INTO zooMercaTiendas (location, owner, admin) VALUES (?,?,?);", 1);
					String locationRaw = loc.getBlockX()+";"+loc.getBlockY()+";"+loc.getBlockZ()+";"+loc.getWorld().getName();
					stmt.setString(1, locationRaw);
					stmt.setString(2, owner.toString());
					stmt.setBoolean(3, admin);
					
					stmt.executeUpdate();
					ResultSet res = stmt.getGeneratedKeys();
					if(res.next()) {
						int idTiendaNew = res.getInt(1);
						tienda.idTienda = idTiendaNew;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	public static Optional<Tienda> getTiendaByLocation(Location location) {
		return tiendas.parallelStream().filter(tienda -> tienda.location.equals(location)).findFirst();
	}
	
	public static int getNumTiendas(UUID owner) {		
		return (int) tiendas.parallelStream().filter(t -> !t.admin && t.owner.equals(owner)).count();
	}
	
	public static Tienda crearTienda(Location loc, UUID owner) {
		return crearTienda(loc, owner, false);
	}
	
	public static Tienda crearTienda(Location loc, UUID owner, boolean admin) {
		Tienda tienda = new Tienda(-1, owner, loc, admin);
		tiendas.add(tienda);
		
		Optional<StockTienda> stockTienda = StockTienda.getStockTiendaByOwner(owner, 0);
		if(!stockTienda.isPresent()) {
			new StockTienda(owner, 0);
		}
		
		return tienda;
	}
	
	public static void tickTiendas() {
		List<Tienda> tiendasBorrar = new ArrayList<Tienda>();
		
		tiendas.stream().forEach(tienda -> {
			if(tienda.haCaducado()) {
				tiendasBorrar.add(tienda);
				return;
			}			
			
			if(!tienda.tieneItems()) {
				return;
			}
			
			if(TradingShops.config.getBoolean("showParticles")) {
				double x = tienda.location.getBlockX() + 0.5;
				double y = tienda.location.getBlockY() + 1.25;
				double z = tienda.location.getBlockZ() + 0.5;
				tienda.location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, x, y, z, 10, 0.1, 0.1, 0.1);
			}
		});
		
		tiendasBorrar.forEach(tienda -> {
			tienda.borrarTienda();
		});
	}
	
	public static void cargarDatos() throws Exception {
		PreparedStatement cargarStocks = null;
		PreparedStatement cargarFilas = null;
		PreparedStatement cargarTiendas = null;
		
		try {
			cargarStocks = TradingShops.getConexion().prepareStatement("SELECT owner, items, pag FROM zooMercaStocks;");
			ResultSet datosStocks = cargarStocks.executeQuery();
			while(datosStocks.next()) {
				String ownerRaw = datosStocks.getString(1);
				UUID owner = UUID.fromString(ownerRaw);
				
				String datosItems = datosStocks.getString(2);
				
				List<ItemStack> itemsList = new ArrayList<ItemStack>();
				
				JsonArray itemsArray = new JsonParser().parse(datosItems).getAsJsonArray();
				itemsArray.forEach(jsonItem -> {
					String itemstackRaw = jsonItem.getAsString();
					YamlConfiguration config = new YamlConfiguration();
					try {
						config.loadFromString(itemstackRaw);
					} catch (InvalidConfigurationException e) {
						e.printStackTrace();
					}
					Map<String, Object> itemRaw = config.getValues(true);
					itemsList.add(ItemStack.deserialize(itemRaw));
				});
				
				int pag = datosStocks.getInt(3);
				
				StockTienda stock = new StockTienda(owner, pag);
				stock.getInventario().setContents(itemsList.toArray(new ItemStack[0]));
			}
			
			cargarTiendas = TradingShops.getConexion().prepareStatement("SELECT location, owner, itemIn, itemOut, idTienda, admin, broadcast FROM zooMercaTiendasFilas LEFT JOIN zooMercaTiendas ON id = idTienda ORDER BY idTienda;");
			ResultSet datosTiendas = cargarTiendas.executeQuery();
			while(datosTiendas.next()) {
				String[] locationRaw = datosTiendas.getString(1).split(";");
				int x = Integer.parseInt(locationRaw[0]);
				int y = Integer.parseInt(locationRaw[1]);
				int z = Integer.parseInt(locationRaw[2]);
				World mundo = Bukkit.getWorld(locationRaw[3]);
				Location location = new Location(mundo, x, y, z);
				
				Optional<Tienda> tienda = Tienda.getTiendaByLocation(location);
				if(!tienda.isPresent()) {
					String ownerRaw = datosTiendas.getString(2);
					UUID owner = UUID.fromString(ownerRaw);					
					
					int idTienda = datosTiendas.getInt(5);
					boolean admin = datosTiendas.getBoolean(6);
					
					tiendas.add(new Tienda(idTienda, owner, location, admin));
					tienda = Tienda.getTiendaByLocation(location);
				}
				
				FilaTienda[] filas = tienda.get().getFilas();
				int index = 0;
				for(int len=filas.length; index<len; index++) {
					FilaTienda fila = filas[index];
					if(fila == null) {
						break;
					}
				}
				if(index >= filas.length) {
					continue;
				}
								
				String itemInstackRaw = datosTiendas.getString(3);
				YamlConfiguration configIn = new YamlConfiguration();
				String itemOutstackRaw = datosTiendas.getString(4);
				YamlConfiguration configOut = new YamlConfiguration();
				try {
					configIn.loadFromString(itemInstackRaw);
					configOut.loadFromString(itemOutstackRaw);
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}
				Map<String, Object> itemInRaw = configIn.getValues(true);
				ItemStack itemIn = ItemStack.deserialize(itemInRaw);
				Map<String, Object> itemOutRaw = configOut.getValues(true);
				ItemStack itemOut = ItemStack.deserialize(itemOutRaw);
				boolean broadcast = datosTiendas.getBoolean(7);
				
				filas[index] = new FilaTienda(itemOut, itemIn, broadcast);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.shutdown();
		} finally {
			try {
				if (cargarStocks != null) {
					cargarStocks.close();
				}
				if (cargarFilas != null) {
					cargarFilas.close();
				}
				if (cargarTiendas != null) {
					cargarTiendas.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void guardarDatos() {
		StockTienda.guardarDatos();
		
		PreparedStatement stmt = null;
		try {
			stmt = TradingShops.getConexion().prepareStatement("DELETE FROM zooMercaTiendasFilas;");
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		tiendas.stream().forEach(tienda -> tienda.guardarDatosTienda());
	}
	
	public boolean tieneItems() {
		for(int i=0; i<filas.length; i++) {
			if(filas[i] != null) {
				return true;
			}
		}
		
		return false;
	}
	
	private void guardarDatosTienda() {
		Arrays.stream(filas).forEach(fila -> {
			if(fila != null) {
				fila.guardarDatos(idTienda);
			}
		});
	}
	
	public boolean comprar(Player player, int index) {
		Optional<FilaTienda> fila = getFila(index);
		if(!fila.isPresent()) {
			return false;
		}
		
		if(!Utils.hasStock(player, fila.get().getItemIn())) {
			player.sendMessage(Mensajes.TIENDA_NO_ITEMS.toString());
			return false;
		}
		if(!Utils.hasStock(this, fila.get().getItemOut())) {
			player.sendMessage(Mensajes.TIENDA_NO_STOCK.toString());
			
			Player ownerPlayer = Bukkit.getPlayer(owner);
			if(ownerPlayer != null && ownerPlayer.isOnline()) {
				ownerPlayer.sendMessage(Mensajes.TIENDA_NO_STOCK_SELF.toString().replaceAll("%s", fila.get().getItemOut().getType().toString()));
			}
			
			return false;
		}
				
		player.getInventory().removeItem(fila.get().getItemIn().clone());
		player.getInventory().addItem(fila.get().getItemOut().clone());	
		
		String nameIn = fila.get().getItemIn().getItemMeta().hasDisplayName() ? fila.get().getItemIn().getItemMeta().getDisplayName() : fila.get().getItemIn().getType().name().replaceAll("_", " ").toLowerCase();
		String nameOut = fila.get().getItemOut().getItemMeta().hasDisplayName() ? fila.get().getItemOut().getItemMeta().getDisplayName() : fila.get().getItemOut().getType().name().replaceAll("_", " ").toLowerCase();
		
		player.sendMessage(Mensajes.TIENDA_COMPRADO.toString()
				.replaceAll("%in", nameOut + " x"+fila.get().getItemOut().getAmount())
				.replaceAll("%out", nameIn + " x"+fila.get().getItemIn().getAmount())
		);
		
		if(!this.admin) {
			this.darItem(fila.get().getItemIn().clone());
			this.cogerItem(fila.get().getItemOut().clone());
			
			Player ownerPlayer = Bukkit.getPlayer(owner);
			if(ownerPlayer != null && ownerPlayer.isOnline()) {
				ownerPlayer.sendMessage(Mensajes.TIENDA_COMPRADO_OWN.toString()
					.replaceAll("%in", nameOut + " x"+fila.get().getItemOut().getAmount())
					.replaceAll("%out", nameIn + " x"+fila.get().getItemIn().getAmount())
					.replaceAll("%p", player.getName()));
			}
		} else if(fila.get().broadcast) {
			Bukkit.broadcastMessage(Mensajes.TIENDA_COMPRADO_OWN.toString()
					.replaceAll("%in", nameOut + " x"+fila.get().getItemOut().getAmount())
					.replaceAll("%out", nameIn + " x"+fila.get().getItemIn().getAmount())
					.replaceAll("%p", player.getName()));
		}
		
		return true;
	}
	
	public boolean haCaducado() {
		if(this.admin) {
			return false;
		}
		
		int maxDias = TradingShops.config.getInt("maxInactiveDays");
		if(maxDias <= 0) {
			return false;
		}
		
		OfflinePlayer player = Bukkit.getOfflinePlayer(this.owner);
		
		long last = player.getLastPlayed();		
		Calendar lastCal = Calendar.getInstance();
		lastCal.setTimeInMillis(last);
		
		Calendar hoyCal = Calendar.getInstance();
		long numDias = ChronoUnit.DAYS.between(lastCal.toInstant(), hoyCal.toInstant());
		
		return numDias >= maxDias;
	}
	
	public void darItem(ItemStack item) {
		ItemStack copy = item.clone();
		
		int max = TradingShops.config.getInt("stockPages");
		for(int i=0; i<max; i++) {
			Optional<StockTienda> stock = StockTienda.getStockTiendaByOwner(this.owner, i);
			if(!stock.isPresent()) {
				continue;
			}
			
			Map<Integer, ItemStack> res = stock.get().getInventario().addItem(copy);
			if(res.isEmpty()) {
				break;
			} else {
				copy.setAmount(res.get(0).getAmount());
			}
		}		
		
		InvStock.getInvStock(this.owner).refrescarItems();
	}
	
	public void cogerItem(ItemStack item) {
		ItemStack copy = item.clone();
		
		int max = TradingShops.config.getInt("stockPages");
		for(int i=0; i<max; i++) {
			Optional<StockTienda> stock = StockTienda.getStockTiendaByOwner(this.owner, i);
			if(!stock.isPresent()) {
				continue;
			}
			
			Map<Integer, ItemStack> res = stock.get().getInventario().removeItem(copy);
			if(res.isEmpty()) {
				break;
			} else {
				copy.setAmount(res.get(0).getAmount());
			}
		}
		
		InvStock.getInvStock(this.owner).refrescarItems();
	}
	
	public void borrar(Player player, int index) {
		filas[index] = null;
	}
	
	public void borrarTienda() {
		borrarTienda(true);
	}
	
	public void borrarTienda(boolean quitarDeArray) {
		if(quitarDeArray) {
			tiendas.remove(this);
			if(TradingShops.config.getBoolean("deleteBlock")) {
				this.location.getBlock().setType(Material.AIR);
			}
		}

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			PreparedStatement stmt1 = null;
			PreparedStatement stmt2 = null;
			try {
				stmt1 = TradingShops.getConexion().prepareStatement("DELETE FROM zooMercaTiendasFilas WHERE idTienda = ?;");
				stmt1.setInt(1, idTienda);
				stmt1.execute();
				
				stmt2 = TradingShops.getConexion().prepareStatement("DELETE FROM zooMercaTiendas WHERE id = ?;");
				stmt2.setInt(1, idTienda);
				stmt2.execute();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (stmt1 != null) {
						stmt1.close();
					}
					if(stmt2 != null) {
						stmt2.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Optional<FilaTienda> getFila(int index) {
		if(index < 0 || index > 3) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(filas[index]);
	}
	
	public FilaTienda[] getFilas() {
		return filas;
	}
	
	public boolean isOwner(UUID owner) {		
		return this.owner.equals(owner);
	}
	
	public boolean isAdmin() {
		return this.admin;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public Location getLocation() {
		return location;
	}
}
