package de.felix.stormboundisles.disasters;

import java.util.List;

/**
 * Represents a disaster definition for an island type.
 */
public class Disaster {
	private final String name;
	private final double chancePerHour;
	private final int durationSeconds;
	private final List<DisasterEffect> effects;

	public Disaster(String name, double chancePerHour, int durationSeconds, List<DisasterEffect> effects) {
		this.name = name;
		this.chancePerHour = chancePerHour;
		this.durationSeconds = durationSeconds;
		this.effects = effects;
	}

	public String getName() {
		return name;
	}

	public double getChancePerHour() {
		return chancePerHour;
	}

	public int getDurationSeconds() {
		return durationSeconds;
	}

	public List<DisasterEffect> getEffects() {
		return effects;
	}
}