package de.felix.stormboundisles.buffs;

/**
 * Represents a passive or temporary buff.
 */
public class Buff {
	private final String type;
	private final int amplifier;
	private final int durationTicks; // 0 = infinite
	private final String[] onlyOnBlocks;

	public Buff(String type, int amplifier, int durationTicks, String[] onlyOnBlocks) {
		this.type = type;
		this.amplifier = amplifier;
		this.durationTicks = durationTicks;
		this.onlyOnBlocks = onlyOnBlocks;
	}

	public String getType() {
		return type;
	}

	public int getAmplifier() {
		return amplifier;
	}

	public int getDurationTicks() {
		return durationTicks;
	}

	public String[] getOnlyOnBlocks() {
		return onlyOnBlocks;
	}
}