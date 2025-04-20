package de.felix.stormboundisles.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Very simple event bus for subscribing to and firing custom events.
 */
public class IslesEventBus<T> {
	private final List<Consumer<T>> listeners = new ArrayList<>();

	public void subscribe(Consumer<T> handler) {
		listeners.add(handler);
	}

	public void fire(T event) {
		for (Consumer<T> handler : listeners) {
			handler.accept(event);
		}
	}
}