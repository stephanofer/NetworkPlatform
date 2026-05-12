package com.stephanofer.networkplatform.menus.internal;

import fr.maxlego08.menu.api.ButtonManager;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ReflectiveButtonCleanup {

    private ReflectiveButtonCleanup() {
    }

    static void collectActionKeys(final ButtonManager buttonManager, final Set<String> destination) {
        final Object value = getField(buttonManager, "actionsLoader");
        if (value instanceof Map<?, ?> map) {
            for (final Object key : map.keySet()) {
                if (key instanceof String string) {
                    destination.add(string);
                }
            }
        }
    }

    static void removeActionKeys(final ButtonManager buttonManager, final Set<String> keys, final Logger logger) {
        removeMapKeys(buttonManager, "actionsLoader", keys, logger);
    }

    static void removePermissibleKeys(final ButtonManager buttonManager, final Set<String> keys, final Logger logger) {
        removeMapKeys(buttonManager, "permissibles", keys, logger);
    }

    private static void removeMapKeys(final ButtonManager buttonManager, final String fieldName, final Set<String> keys, final Logger logger) {
        final Object value = getField(buttonManager, fieldName);
        if (!(value instanceof Map<?, ?> rawMap)) {
            return;
        }

        @SuppressWarnings("unchecked")
        final Map<String, ?> map = (Map<String, ?>) rawMap;
        for (final String key : keys) {
            map.remove(key);
        }

        if (!keys.isEmpty()) {
            logger.fine("Removed " + keys.size() + " zMenu " + fieldName + " entries via reflective cleanup");
        }
    }

    private static Object getField(final Object target, final String fieldName) {
        try {
            final Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (final ReflectiveOperationException ignored) {
            return null;
        }
    }
}
