package de.nofelix.stormboundisles.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an initialization method that should be automatically called
 * during the mod's startup sequence.
 * <p>
 * Methods marked with this annotation must be static, have no parameters, and return void.
 * They will be discovered and called by the {@link InitializationRegistry} during mod initialization.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Initialize(priority = 100)
 * public static void initialize() {
 *     // Initialization code
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Initialize {
    /**
     * The priority of this initialization method. Methods with higher priority
     * will be executed before methods with lower priority.
     * <p>
     * Default priority is 1000. Range is 0-9999.
     * 
     * @return The initialization priority
     */
    int priority() default 1000;
    
    /**
     * Optional description of what this initialization method does.
     * For documentation purposes only.
     * 
     * @return Description of the initialization method
     */
    String description() default "";
}