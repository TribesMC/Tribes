package me.rey.clans.utils;

import me.rey.clans.Tribes;
import org.bukkit.Material;

public class References {

    /*
     * CLAN VARIABLES
     */
    public static final int MAX_ALLIES = 2;
    public static final int MAX_TRUCES = 1;
    public static final int MAX_MEMBERS = 9;
    public static final int MAX_TERRITORY = 8;
    public static final long MAX_ENERGY = 10000;
    public static final double MAX_ENERGY_DAYS = 7.0;
    public static final Material HOME_BLOCK = Material.BEACON;
    public static final int DEFAULT_ENERGY = (int) (References.MAX_ENERGY / References.MAX_ENERGY_DAYS);

    /*
     * SIEGE VARIABLES
     */
    public static final int SIEGE_MINUTES = 30;
    public static final double FIELDS_ORE_COOLDOWN_SECONDS = 1.0 * 60;
    public static final double CLAN_HOME_COOLDOWN_SECONDS = 5.0 * 60;
    public static final double CLAN_JOIN_COOLDOWN_SECONDS = 10.0 * 60;
    /*
     * MISC
     */
    private static final String STAFF_RANK_NAME = Tribes.getInstance().getPlugin().getConfig().getString("staff-permission-rank");

	/* TODO: Get default staff rank
	public static UserRank getStaffRank() {
		for(UserRank ur : UserRank.values()) {
			if(ur.getName().equalsIgnoreCase(staffRankName))
				return ur;
		}
		return UserRank.ADMIN;
	}
	*/

}
