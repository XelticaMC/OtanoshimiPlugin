package work.xeltica.craft.core.travels;

public enum TicketType {
    WILDAREA("ワイルドエリア"),
    MEGAWILD("メガワイルド"),
    MAIKURA_CITY("舞倉市"),
    MOON("月"),
    ;

    public String getDisplayName() {
        return this.displayName;
    }

    private TicketType(String displayName) {
        this.displayName = displayName;
    }

    private String displayName;
}
