package de.felix.stormboundisles.zones;


import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Represents an island zone (e.g., polygonal zone for a team).
 */
public class IslandZone {
	private final String teamName;
	private final List<BlockPos> polygonPoints;
	private final String islandType; // e.g., "volcano", "desert", etc.

	public IslandZone(String teamName, List<BlockPos> polygonPoints, String islandType) {
		// TODO: Eingabevalidierung im Konstruktor implementieren (teamName nicht leer, mindestens 3 Polygonpunkte)
		this.teamName = teamName;
		this.polygonPoints = List.copyOf(polygonPoints);
		this.islandType = islandType;
	}

	/**
	 * Checks if a given position is inside the polygon defined by the island zone.
	 *
	 * @param pos The position to check.
	 * @return true if the position is inside the polygon, false otherwise.
	 */
	public boolean isInside(BlockPos pos) {
		// TODO: Höhenbegrenzungen (minY, maxY) hinzufügen, um vertikale Grenzen der Zone zu definieren
		// TODO: Bounding-Box-Berechnung für Performance-Optimierung implementieren (minX, maxX, minZ, maxZ)
		// TODO: Methode getCenter() implementieren, um den Mittelpunkt der Zone zu berechnen
		int x = pos.getX();
		int z = pos.getZ();
		int n = polygonPoints.size();
		boolean inside = false;
		for (int i = 0, j = n -1; i < n; j = i++) {
			int xi = polygonPoints.get(i).getX(), zi = polygonPoints.get(i).getZ();
			int xj = polygonPoints.get(j).getX(), zj = polygonPoints.get(j).getZ();

			boolean intersect = ((zi > z) != (zj > z))
					&& (x < (xj - xi) * (z - zi) / (zj - zi + 0.0001) + xi);
			if (intersect) inside = !inside;
		}
		return inside;
	}

	// TODO: Methode isNearBorder(BlockPos, int) hinzufügen, um zu prüfen, ob ein Punkt nahe der Zonengrenze ist

	public String getTeamName() {
		return teamName;
	}

	public List<BlockPos> getPolygonPoints() {
		return  List.copyOf(polygonPoints);
	}

	public String getIslandType() {
		return islandType;
	}
}
