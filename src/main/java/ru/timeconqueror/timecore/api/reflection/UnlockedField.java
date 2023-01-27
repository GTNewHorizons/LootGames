package ru.timeconqueror.timecore.api.reflection;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

/**
 * Wrapper for field, unlocks the access to it.
 *
 * @param <T> field type.
 */
public class UnlockedField<T> {
    private final Field field;
    private boolean finalized;

    public UnlockedField(Field field) {
        this.field = field;

        ReflectionHelper.setAccessible(field);
        finalized = ReflectionHelper.isFinal(field);
    }

    /**
     * Gets the value of field in provided {@code fieldOwner}
     * Safe for use with non-accessible fields.
     *
     * @param fieldOwner owner of field. If the underlying field is static, the obj argument is ignored; it may be null.
     */
    @SuppressWarnings("unchecked")
    public T get(@Nullable Object fieldOwner) {
        try {
            return (T) field.get(fieldOwner);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Field getField() {
        return field;
    }

    /**
     * Returns true, if provided field is static, otherwise returns false.
     */
    public boolean isStatic() {
        return ReflectionHelper.isStatic(field);
    }

    @Override
    public String toString() {
        return field.toString();
    }
}
