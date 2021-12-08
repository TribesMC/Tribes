package me.rey.core.classes;

import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public abstract class ClassCondition implements Listener {

	private static final Map<ClassType, List<ClassCondition>> classConditions = new HashMap<>();

	public static List<ClassCondition> getClassConditions(ClassType classType) {
		return Optional.ofNullable(classConditions.get(classType)).orElse(Collections.<ClassCondition>emptyList());
	}

	public static void registerCondition(ClassCondition condition) {
		final ClassType classType = condition.getClassType();
		final List<ClassCondition> currentConditions = classConditions.getOrDefault(classType, new ArrayList<>());
		currentConditions.add(condition);
		classConditions.put(classType, currentConditions);
	}
	
	private final ClassType classType;
	private final String name;
	private final List<String> description;
	
	public ClassCondition(ClassType classType, String name, List<String> description) {
		this.classType = classType;
		this.name = name;
		this.description = description;
	}

	public ClassCondition(ClassType classType, String name) {
		this(classType, name, new ArrayList<>());
	}
	
	public ClassType getClassType() {
		return classType;
	}

	public List<String> getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	@EventHandler
	public void onUpdate(UpdateEvent e) {
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(new User(p).getWearingClass() != this.classType) continue;
			
			this.execute(new User(p), p);
		}
	}
	
	protected abstract void execute(User user, Player player);

}
