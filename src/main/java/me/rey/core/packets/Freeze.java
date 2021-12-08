package me.rey.core.packets;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

public class Freeze extends Packets {

	@Override
	public void send(LivingEntity entity) {
		net.minecraft.server.v1_8_R3.Entity nmsEn = ((CraftEntity) entity).getHandle();
	    NBTTagCompound compound = new NBTTagCompound();
	    nmsEn.c(compound);
	    compound.setByte("NoAI", (byte) 1);
	    nmsEn.f(compound);
	}

}
