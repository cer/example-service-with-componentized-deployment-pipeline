package io.eventuate.customerservice.customermanagement.eventpublishing;

import java.lang.reflect.Field;

public class EntityIdSetter {

    public static void setId(Object entity, Object id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set id on " + entity.getClass().getSimpleName(), e);
        }
    }
}
