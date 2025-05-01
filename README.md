# Stormbound Isles

**Stormbound Isles** ist ein Minecraft-Projekt auf Basis von Fabric für Version 1.21.1.  
Fünf Teams treten auf fünf einzigartigen, elementar inspirierten Inseln gegeneinander an, bauen, kämpfen und überleben – während zufällige Katastrophen das Spielgeschehen beeinflussen.

Ziel ist es, die eigene Insel zu gestalten, als Team zu überleben und am Ende sowohl durch Kreativität als auch durch strategischen Kampf zu punkten.

## Features

- 5 Elementar-Inseln: Vulkan, Eis/Schnee, Wüste, Pilz, Kristall/Magie
- Katastrophen, die zufällig oder durch Events ausgelöst werden und das Gameplay beeinflussen
- Team-Passivboni abhängig von Insel und Position
- Bewertungsphase: Punkte für Bauwerke, Überleben und Kreativität – inkl. Jury-System
- PvP-Phase nach Ablauf der „Schutzwoche" – dann sind Überfälle und Kämpfe möglich
- Modpack basierend auf Fabric (u.a. Simple Voice Chat, Sodium, Create, Iris, ...)

## Befehle

Stormbound Isles bietet verschiedene Befehle, die über `/sbi` aufgerufen werden können:

### Admin-Befehle (Berechtigungsstufe 3)
- `/sbi admin game start` - Startet das Spiel
- `/sbi admin game stop` - Stoppt das Spiel
- `/sbi admin game phase <Phase>` - Setzt die aktuelle Spielphase
- `/sbi admin reset` - Setzt alle Spieldaten zurück (mit Bestätigung)

### Insel-Befehle (Berechtigungsstufe 2)
- `/sbi island list` - Zeigt alle Inseln an
- `/sbi island setspawn <InselID>` - Setzt den Spawn-Punkt einer Insel
- `/sbi island disaster trigger <InselID> <Typ>` - Löst eine Katastrophe aus
- `/sbi island zone ...` - Verwaltet Inselzonen (Polygon, Rechteck)

### Team-Befehle
- `/sbi team assign <TeamName> <Spieler>` - Weist einen Spieler einem Team zu (Level 2)
- `/sbi team remove <Spieler>` - Entfernt einen Spieler aus seinem Team (Level 2)
- `/sbi team info <TeamName>` - Zeigt Informationen über ein Team (Level 0)

### Punkte-Befehle (Berechtigungsstufe 2)
- `/sbi points add <Team> <Anzahl> [Grund]` - Fügt einem Team Punkte hinzu
- `/sbi points remove <Team> <Anzahl> [Grund]` - Entfernt Punkte von einem Team

### Spieler-Befehle (Berechtigungsstufe 0)
- `/sbi player info [Spieler]` - Zeigt Informationen über einen Spieler

## Entwicklung

Das Plugin verwendet eine modulare Befehlsstruktur, die in verschiedene Kategorien aufgeteilt ist. Jede Befehlskategorie ist in einer eigenen Klasse implementiert, was die Wartung und Erweiterung erleichtert.

### Berechtigungsstufen
- Level 0: Reguläre Spieler
- Level 2: Moderatoren
- Level 3: Administratoren
  
<p align="right">
  <img src="src/main/resources/assets/stormbound-isles/icon.png" alt="Stormbound Isles Icon" width="48" />
</p>
