package es.elzoo.tradingshops_14;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public class StockTienda {
	private static List<StockTienda> stocks = new ArrayList<StockTienda>();

	private UUID owner;
	private Inventory inventario;
	private int pag;

	public StockTienda(UUID owner, int pag) {
		this(owner, Bukkit.createInventory(null, 45, ChatColor.GREEN + Bukkit.getOfflinePlayer(owner).getName()+"'s shop"), pag);
	}

	public StockTienda(UUID owner, Inventory inv, int pag) {
		this.owner = owner;
		this.inventario = inv;
		this.pag = pag;

		stocks.add(this);
	}

	public static Optional<StockTienda> getStockTiendaByOwner(UUID owner, int pag) {
		return stocks.parallelStream().filter(t -> t.owner.equals(owner) && t.pag == pag).findFirst();
	}
	
	public static void guardarDatos() {
		if(!hayStock()) {
			return;
		}
		
		PreparedStatement stmt = null;
		try {
			stmt = TradingShops.getConexion().prepareStatement("DELETE FROM zooMercaStocks;");
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
		
		stocks.stream().forEach(stock -> stock.guardarDatosStock());
	}
	
	private static boolean hayStock() {
		return (int) stocks.parallelStream().filter(stock -> {
			return Arrays.asList(stock.getInventario().getContents()).parallelStream().anyMatch(item -> {
				return item != null && item.getAmount() > 0;
			});
		}).count() > 0;
	}
	
	private void guardarDatosStock() {
		PreparedStatement stmt = null;
		try {
			stmt = TradingShops.getConexion().prepareStatement("INSERT INTO zooMercaStocks (owner, items, pag) VALUES (?,?,?);");
			
			JsonArray items = new JsonArray();			
			Arrays.stream(inventario.getContents()).forEach(item -> {
				if(item == null) {
					return;
				}
				
				YamlConfiguration config = new YamlConfiguration();
				item.serialize().forEach((s,o) -> {config.set(s, o);});
				String itemRaw = config.saveToString();	
				items.add(itemRaw);
			});			
			String itemsJson = (new Gson()).toJson(items);
			
			stmt.setString(1, owner.toString());
			stmt.setString(2, itemsJson);
			stmt.setInt(3, pag);
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
	}

	public Inventory getInventario() {
		return inventario;
	}
	
	public void setInventario(Inventory inventario) {
		for(int i=0; i<45; i++) {
			this.inventario.setItem(i, inventario.getItem(i));
		}
	}

	public UUID getOwner() {
		return owner;
	}
}
