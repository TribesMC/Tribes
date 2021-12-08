package me.rey.clans.enums;

import me.rey.core.utils.Text;

public enum CommandType {
	
	HELP(),
	STAFF(),
	TEST(),
	FEATURE(),
	CLAN();
	
	public String getName() {
		return Text.formatName(this.name());
	}
	
}
