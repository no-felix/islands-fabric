package de.felix.stormboundisles.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
	public static void info(String msg) {
		System.out.println("[StormboundIsles][INFO] " + now() + " " + msg);
	}

	public static void warn(String msg) {
		System.out.println("[StormboundIsles][WARN] " + now() + " " + msg);
	}

	public static void error(String msg) {
		System.err.println("[StormboundIsles][ERROR] " + now() + " " + msg);
	}

	private static String now() {
		return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
	}
}