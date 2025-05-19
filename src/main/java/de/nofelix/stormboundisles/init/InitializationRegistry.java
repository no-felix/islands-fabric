package de.nofelix.stormboundisles.init;

import de.nofelix.stormboundisles.StormboundIslesMod;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registry for discovering and calling initialization methods annotated with
 * {@link Initialize}.
 * <p>
 * This class scans for methods marked with the {@link Initialize} annotation
 * and executes them in order of priority (higher priority executes first).
 * <p>
 * Methods must be static, have no parameters, and return void to be eligible
 * for automatic
 * initialization.
 */
public class InitializationRegistry {
    private static boolean initialized = false;

    /**
     * Discovers and executes all initialization methods in the given package and
     * its subpackages.
     * Methods are executed in order of their priority (higher priority first).
     *
     * @param basePackage The base package to scan for initialization methods
     */
    public static void initializeAll(String basePackage) {
        if (initialized) {
            StormboundIslesMod.LOGGER.warn("InitializationRegistry.initializeAll() called more than once");
            return;
        }

        StormboundIslesMod.LOGGER.info("Scanning for initialization methods in package: {}", basePackage);

        try {
            // Create a Reflections scanner to find annotated methods
            Reflections reflections = new Reflections(basePackage, Scanners.MethodsAnnotated);

            // Find all methods annotated with @Initialize
            Set<Method> initMethods = reflections.getMethodsAnnotatedWith(Initialize.class);

            if (initMethods.isEmpty()) {
                StormboundIslesMod.LOGGER.warn("No initialization methods found in package: {}", basePackage);
                return;
            }

            // Filter out non-static or methods with parameters - using modern pattern
            // matching
            Set<Method> validMethods = initMethods.stream()
                    .filter(method -> Modifier.isStatic(method.getModifiers()))
                    .filter(method -> method.getParameterCount() == 0)
                    .filter(method -> method.getReturnType() == void.class)
                    .collect(Collectors.toSet());

            if (initMethods.size() != validMethods.size()) {
                StormboundIslesMod.LOGGER.warn("{} initialization methods were invalid and will be skipped: {}",
                        initMethods.size() - validMethods.size(),
                        initMethods.stream()
                                .filter(m -> !validMethods.contains(m))
                                .map(m -> m.getDeclaringClass().getSimpleName() + "." + m.getName())
                                .collect(Collectors.joining(", ")));
            }

            // Sort by priority (higher priority first) - using modern method reference
            validMethods.stream()
                    .sorted(Comparator.comparingInt(InitializationRegistry::getPriorityInverse))
                    .forEach(method -> {
                        Initialize annotation = method.getAnnotation(Initialize.class);
                        String description = annotation.description().isEmpty()
                                ? ""
                                : " - " + annotation.description();

                        try {
                            StormboundIslesMod.LOGGER.debug("Calling initialization method: {}.{}{}",
                                    method.getDeclaringClass().getSimpleName(),
                                    method.getName(),
                                    description);

                            method.invoke(null);
                            StormboundIslesMod.LOGGER.trace("Successfully called {}.{}",
                                    method.getDeclaringClass().getSimpleName(), method.getName());
                        } catch (Exception e) {
                            StormboundIslesMod.LOGGER.error("Failed to call initialization method: {}.{}",
                                    method.getDeclaringClass().getSimpleName(), method.getName(), e);
                        }
                    });

            initialized = true;
            StormboundIslesMod.LOGGER.info("Successfully initialized {} methods", validMethods.size());
        } catch (Exception e) {
            StormboundIslesMod.LOGGER.error("Failed to scan for initialization methods", e);
        }
    }

    /**
     * Gets the inverse priority value for sorting (higher priority methods run
     * first).
     * 
     * @param method The method to get priority for
     * @return The negative priority value (for sorting in descending order)
     */
    private static int getPriorityInverse(Method method) {
        Initialize annotation = method.getAnnotation(Initialize.class);
        return -annotation.priority(); // Negative to sort higher values first
    }

    /**
     * Resets the initialization status, allowing initializeAll to be called again.
     * This is primarily for testing purposes.
     */
    public static void reset() {
        initialized = false;
    }
}