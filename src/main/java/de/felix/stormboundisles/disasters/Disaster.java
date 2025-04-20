package de.felix.stormboundisles.disasters;

import java.util.List;
import java.util.Map;

/**
 * Represents a disaster definition for an island type.
 */
public class Disaster {
	private final String name;
	private final double chancePerHour;
	private final int durationMinutes;
	private final List<Map<String, Object>> effects; // effect type + params

	public Disaster(String name, double chancePerHour, int durationMinutes, List<Map<String, Object>> effects) {
		this.name = name;
		this.chancePerHour = chancePerHour;
		this.durationMinutes = durationMinutes;
		this.effects = effects;
	}

	public String getName() {
		return name;
	}

	public double getChancePerHour() {
		return chancePerHour;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public List<Map<String, Object>> getEffects() {
		return effects;
	}
}