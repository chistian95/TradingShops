package es.elzoo.tradingshops;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import es.elzoo.tradingshops.Mensajes;
import es.elzoo.tradingshops.Permisos;
import es.elzoo.tradingshops.inventarios.InvAdminTienda;
import es.elzoo.tradingshops.inventarios.InvStock;
import es.elzoo.tradingshops.inventarios.InvTienda;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EventosTienda implements Listener {
	public static List<String> jugadoresCreando = new ArrayList<String>();
	public static List<String> jugadoresCreandoAdmin = new ArrayList<String>();
	public static List<String> jugadoresBorrando = new ArrayList<String>();
	
	@EventHandler
	public void onPlayerInteractStock(PlayerInteractEvent event) {
		Block bloque = event.getClickedBlock();
		String stockBlock = TradingShops.config.getString("stockBlock");
		
		Material match = Material.matchMaterial(stockBlock);
		if(match == null) {
			try {
				match = Material.matchMaterial(stockBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception e) {
				match = null;
			}
			
			if(match == null) {
				match = Material.JUKEBOX;
			}
		}
		
		if(bloque == null || !bloque.getType().equals(match)) {
			return;
		}
		
		boolean isTiendaLoc = false;
		
		if(TradingShops.wgLoader != null) {
			isTiendaLoc = TradingShops.wgLoader.checkRegion(bloque);
		} else {
			isTiendaLoc = true;
		}
		
		if(!isTiendaLoc) {
			return;
		}
		
		if(event.getPlayer().isSneaking() && event.getPlayer().hasPermission(Permisos.TIENDA_ROMPER_STOCK.toString())) {
			return;
		}
		
		event.setCancelled(true);
		
		InvStock inv = InvStock.getInvStock(event.getPlayer().getUniqueId());
		inv.setPag(0);
		inv.abrir(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block bloque = event.getClickedBlock();
		String shopBlock = TradingShops.config.getString("shopBlock");
		
		Material match = Material.matchMaterial(shopBlock);
		if(match == null) {
			try {
				match = Material.matchMaterial(shopBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception e) {
				match = null;
			}
			
			if(match == null) {
				match = Material.CHEST;
			}
		}
		
		if(bloque == null || !bloque.getType().equals(match)) {
			jugadoresCreando.remove(event.getPlayer().getName());
			jugadoresCreandoAdmin.remove(event.getPlayer().getName());
			jugadoresBorrando.remove(event.getPlayer().getName());
			
			return;
		}
		
		boolean isTiendaLoc = false;
		
		if(TradingShops.wgLoader != null) {
			isTiendaLoc = TradingShops.wgLoader.checkRegion(bloque);
		} else {
			isTiendaLoc = true;
		}
		
		Optional<Tienda> tienda = Tienda.getTiendaByLocation(bloque.getLocation());	
		
		if(!tienda.isPresent()) {
			if(!isTiendaLoc) {
				return;				
			}						
			
			if(jugadoresCreando.contains(event.getPlayer().getName())) {
				event.setCancelled(true);
				
				jugadoresCreando.remove(event.getPlayer().getName());
				
				int maxTiendas = 0;
				String permPrefix = Permisos.TIENDA_LIMITE_PREFIX.toString();				
				for(PermissionAttachmentInfo attInfo : event.getPlayer().getEffectivePermissions()) {
					String perm = attInfo.getPermission();
					if(perm.startsWith(permPrefix)) {	
						int num = 0;
						try {
							num = Integer.parseInt(perm.substring(perm.lastIndexOf(".")+1));
						} catch(Exception e) {
							num = 0;
						}
						
						if(num > maxTiendas) {
							maxTiendas = num;
						}
					}
				}
				
				boolean limiteTiendas = true;
				int numTiendas = Tienda.getNumTiendas(event.getPlayer().getUniqueId());				
				
				if(TradingShops.config.getBoolean("usePermissions")) {
					if(numTiendas >= maxTiendas) {
						limiteTiendas = true;
					} else {
						limiteTiendas = false;
					}
				} else {
					int numConfig = TradingShops.config.getInt("defaultShopLimit");
					if(numTiendas >= numConfig && numConfig >= 0) {
						limiteTiendas = true;
					} else {
						limiteTiendas = false;
					}
				}
				
				if(event.getPlayer().isOp()) {
					limiteTiendas = false;
				}
				
				if(event.getPlayer().hasPermission(Permisos.TIENDA_LIMITE_BYPASS.toString())) {
					limiteTiendas = false;
				}
				
				if(limiteTiendas) {
					event.getPlayer().sendMessage(Mensajes.TIENDA_MAX.toString());
					return;
				}
				
				double coste = TradingShops.config.getDouble("createCost");
				Optional<Economy> economy = TradingShops.getEconomy();
				if(coste > 0 && economy.isPresent()) {
					OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId());		
					EconomyResponse res = economy.get().withdrawPlayer(offPlayer, coste);
					
					if(!res.transactionSuccess()) {
						event.getPlayer().sendMessage(Mensajes.TIENDA_CREATE_NO_MONEY.toString()+coste);
						return;
					}
				}
				
				Tienda nuevaTienda = Tienda.crearTienda(bloque.getLocation(), event.getPlayer().getUniqueId());
				event.getPlayer().sendMessage(Mensajes.TIENDA_CREADA.toString());
				
				InvAdminTienda inv = new InvAdminTienda(nuevaTienda);
				inv.abrir(event.getPlayer());				
			} else if(jugadoresCreandoAdmin.contains(event.getPlayer().getName())) {
				event.setCancelled(true);
				
				jugadoresCreandoAdmin.remove(event.getPlayer().getName());
				
				if(!event.getPlayer().isOp() || !event.getPlayer().hasPermission(Permisos.TIENDA_ADMIN.toString())) {
					event.getPlayer().sendMessage(Mensajes.NO_PERMISOS.toString());
					return;
				}
				
				Tienda nuevaTienda = Tienda.crearTienda(bloque.getLocation(), event.getPlayer().getUniqueId(), true);
				event.getPlayer().sendMessage(Mensajes.TIENDA_CREADA.toString());
				
				InvAdminTienda inv = new InvAdminTienda(nuevaTienda);
				inv.abrir(event.getPlayer());	
			}
			
			jugadoresCreando.remove(event.getPlayer().getName());
			jugadoresCreandoAdmin.remove(event.getPlayer().getName());
			jugadoresBorrando.remove(event.getPlayer().getName());
			
			return;
		}		
		
		event.setCancelled(true);
		
		if(jugadoresBorrando.contains(event.getPlayer().getName())) {
			jugadoresCreando.remove(event.getPlayer().getName());
			jugadoresCreandoAdmin.remove(event.getPlayer().getName());
			jugadoresBorrando.remove(event.getPlayer().getName());			
			
			if(tienda.get().isAdmin()) {
				if(!event.getPlayer().hasPermission(Permisos.TIENDA_ADMIN.toString())) {
					event.getPlayer().sendMessage(Mensajes.NO_PERMISOS.toString());
					return;
				}				
			} else if(!tienda.get().isOwner(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission(Permisos.TIENDA_ADMIN.toString())) {
				event.getPlayer().sendMessage(Mensajes.TIENDA_NO_SELF.toString());
				return;
			}
			
			double coste = TradingShops.config.getDouble("returnAmount");
			Optional<Economy> economy = TradingShops.getEconomy();
			if(coste > 0 && economy.isPresent()) {
				OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId());		
				economy.get().depositPlayer(offPlayer, coste);
			}
			
			tienda.get().borrarTienda();
			event.getPlayer().sendMessage(Mensajes.TIENDA_BORRADA.toString());
			
			return;
		}
		
		if((tienda.get().isAdmin() && event.getPlayer().hasPermission(Permisos.TIENDA_ADMIN.toString())) || tienda.get().isOwner(event.getPlayer().getUniqueId())) {
			InvAdminTienda inv = new InvAdminTienda(tienda.get());
			inv.abrir(event.getPlayer());
		} else {
			InvTienda inv = new InvTienda(tienda.get());
			inv.abrir(event.getPlayer());
		}
		
		jugadoresCreando.remove(event.getPlayer().getName());
		jugadoresCreandoAdmin.remove(event.getPlayer().getName());
		jugadoresBorrando.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		jugadoresCreando.remove(event.getPlayer().getName());
		jugadoresCreandoAdmin.remove(event.getPlayer().getName());
		jugadoresBorrando.remove(event.getPlayer().getName());
	}
}
