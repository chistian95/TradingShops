package es.elzoo.tradingshops;

import org.bukkit.ChatColor;

public enum Mensajes {
	NO_PERMISOS("noPermissions"),
	
	TIENDA_BORRAR("deleteShop"),
	TIENDA_CREAR("createShop"),
	TIENDA_NO_SELF("shopNotOwned"),
	TIENDA_BORRADA("shopDeleted"),
	TIENDA_MAX("shopLimit"),
	TIENDA_CREADA("shopCreated"),
	TIENDA_NO_ITEMS("noItems"),
	TIENDA_NO_STOCK("noStock"),
	TIENDA_NO_STOCK_SELF("noStockNotify"),
	TIENDA_COMPRADO("buy"),
	TIENDA_COMPRADO_OWN("sell"),
	TIENDA_RELODADED("reload"),
	TIENDA_CREATE_NO_MONEY("noMoney"),
	TIENDA_TITLE_ADMIN_SHOP("adminShop"),
	TIENDA_TITLE_NORMAL_SHOP("normalShop"),
	TIENDA_TITLE_SELL("sellTitle"),
	TIENDA_TITLE_BUY("buyTitle"),
	TIENDA_TITLE_DELETE("deleteTitle"),
	TIENDA_TITLE_CREATE("createTitle"),
	TIENDA_TITLE_BROADCAST_ON("broadcastOn"),
	TIENDA_TITLE_BROADCAST_OFF("broadcastOff"),
	TIENDA_TITLE_CREATESHOP("createShopTitle"),
	TIENDA_TITLE_STOCK("stockTitle"),
	TIENDA_PAGE("page"),
	TIENDA_TITLE_BUYACTION("buyAction");
	
	private String msg;
	
	private Mensajes(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		String trans = TradingShops.config.getString(msg);
		return ChatColor.translateAlternateColorCodes('&', trans);
	}
}