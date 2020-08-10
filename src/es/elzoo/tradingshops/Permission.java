package es.elzoo.tradingshops;

public enum Permission {
	SHOP_ADMIN("tradingshop.admin"),
	SHOP_CREATE("tradingshop.create"),
	SHOP_LIMIT_BYPASS("tradingshop.create.limit.bypass"),
	SHOP_LIMIT_PREFIX("tradingshop.create.limit.");

	private final String perm;

	Permission(String perms) {
		this.perm = perms;
	}

	@Override
	public String toString() {
		return perm;
	}
}
