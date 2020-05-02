package es.elzoo.tradingshops_14;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import es.elzoo.tradingshops_14.inventarios.InvStock;
import net.md_5.bungee.api.ChatColor;

public class ComandoTienda implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		
		if(args.length == 0) {
			listaSubCmd(player, label);
		} else if(args[0].equalsIgnoreCase("create")) {
			crearTienda(player);
		} else if(args[0].equalsIgnoreCase("adminshop")) {
			adminShop(player);
		} else if(args[0].equalsIgnoreCase("delete")) {
			borrarTienda(player);
		} else if(args[0].equalsIgnoreCase("stock")) {
			stockTienda(player);
		} else if(args[0].equalsIgnoreCase("reload")) {
			reloadTienda(player);
		} else {
			listaSubCmd(player, label);
		}
		
		return true;
	}
	
	private void listaSubCmd(Player player, String label) {
		player.sendMessage(ChatColor.GRAY + "/"+label+" create");
		player.sendMessage(ChatColor.GRAY + "/"+label+" adminshop");
		player.sendMessage(ChatColor.GRAY + "/"+label+" delete");
		player.sendMessage(ChatColor.GRAY + "/"+label+" stock");
	}
	
	private void crearTienda(Player player) {		
		if(!EventosTienda.jugadoresCreando.contains(player.getName())) {
			EventosTienda.jugadoresCreando.add(player.getName());			
		}		
		EventosTienda.jugadoresBorrando.remove(player.getName());
		
		player.sendMessage(Mensajes.TIENDA_CREAR.toString());
	}
	
	private void adminShop(Player player) {		
		if(!EventosTienda.jugadoresCreandoAdmin.contains(player.getName())) {
			EventosTienda.jugadoresCreandoAdmin.add(player.getName());			
		}		
		EventosTienda.jugadoresBorrando.remove(player.getName());
		
		player.sendMessage(Mensajes.TIENDA_CREAR.toString());
	}
	
	private void borrarTienda(Player player) {
		if(!EventosTienda.jugadoresBorrando.contains(player.getName())) {
			EventosTienda.jugadoresBorrando.add(player.getName());			
		}		
		EventosTienda.jugadoresCreando.remove(player.getName());
		
		player.sendMessage(Mensajes.TIENDA_BORRAR.toString());
	}
	
	private void stockTienda(Player player) {
		if(!TradingShops.config.getBoolean("enableStockCommand")) {
			player.sendMessage(Mensajes.NO_PERMISOS.toString());
			return;
		}
		
		InvStock inv = InvStock.getInvStock(player.getUniqueId());
		inv.abrir(player);
	}
	
	private void reloadTienda(Player player) {
		if(!player.hasPermission(Permisos.TIENDA_ADMIN.toString())) {
			player.sendMessage(Mensajes.NO_PERMISOS.toString());
			return;
		}
		
		TradingShops plugin = (TradingShops) Bukkit.getPluginManager().getPlugin("TradingShops");
		plugin.crearConfig();
		player.sendMessage(Mensajes.TIENDA_RELODADED.toString());
	}
}
