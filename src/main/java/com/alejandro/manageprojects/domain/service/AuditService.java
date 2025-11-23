package com.alejandro.manageprojects.domain.service;

import com.alejandro.manageprojects.view.AuditUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditService {

    private final AuditUtils auditUtils;

    private static final String ENTITY_PKG = "com.alejandro.manageprojects.domain.entity";

    private static final Map<String, Class<?>> ENTITY_BY_NAME;
    static {
        Map<String, Class<?>> m = new HashMap<>();
        try {
            // Registra aquí las entidades auditadas conocidas
            m.put("proyecto", Class.forName(ENTITY_PKG + ".Proyecto"));
            m.put("tarea", Class.forName(ENTITY_PKG + ".Tarea"));
            m.put("usuario", Class.forName(ENTITY_PKG + ".Usuario"));
            m.put("comentario", Class.forName(ENTITY_PKG + ".Comentario"));
            m.put("etiqueta", Class.forName(ENTITY_PKG + ".Etiqueta"));
            m.put("hito", Class.forName(ENTITY_PKG + ".Hito"));
            m.put("presupuesto", Class.forName(ENTITY_PKG + ".Presupuesto"));
            m.put("miembroproyecto", Class.forName(ENTITY_PKG + ".MiembroProyecto"));
            m.put("perfil", Class.forName(ENTITY_PKG + ".Perfil"));
            m.put("rol", Class.forName(ENTITY_PKG + ".Rol"));
        } catch (ClassNotFoundException e) {
            // No debería ocurrir en build; si falta alguna clase, se verá rápidamente
        }
        ENTITY_BY_NAME = Collections.unmodifiableMap(m);
    }

    public AuditService(AuditUtils auditUtils) {
        this.auditUtils = auditUtils;
    }

    public Class<?> resolveEntityClass(String entityName) {
        if (entityName == null) throw new IllegalArgumentException("Nombre de entidad vacío");
        String key = entityName.trim().toLowerCase(Locale.ROOT);
        Class<?> clazz = ENTITY_BY_NAME.get(key);
        if (clazz == null) {
            throw new IllegalArgumentException("Entidad desconocida: " + entityName);
        }
        return clazz;
    }

    public List<Number> getRevisionNumbers(String entity, Long id) {
        Class<?> clazz = resolveEntityClass(entity);
        return auditUtils.getRevisionNumbers(clazz, id);
    }

    public Map<Number, LocalDateTime> getRevisionTimestamps(String entity, Long id) {
        Class<?> clazz = resolveEntityClass(entity);
        return auditUtils.getRevisionTimestamps(clazz, id);
    }

    public Map<String, Object> getAtRevision(String entity, Long id, Number rev) {
        Class<?> clazz = resolveEntityClass(entity);
        Object snapshot = auditUtils.getAtRevision(clazz, id, rev);
        return toFlatMap(snapshot);
    }

    public Map<String, Object> getAtDate(String entity, Long id, LocalDateTime at) {
        Class<?> clazz = resolveEntityClass(entity);
        Object snapshot = auditUtils.getAtDate(clazz, id, at);
        return toFlatMap(snapshot);
    }

    public List<Map<String, Object>> getBetween(String entity, Long id, LocalDateTime from, LocalDateTime to) {
        Class<?> clazz = resolveEntityClass(entity);
        List<?> snapshots = auditUtils.getRevisionsBetween(clazz, id, from, to);
        List<Map<String, Object>> list = new ArrayList<>(snapshots.size());
        for (Object s : snapshots) {
            list.add(toFlatMap(s));
        }
        return list;
    }

    // --- Helpers para aplanar entidades en estructuras seguras para JSON ---
    private Map<String, Object> toFlatMap(Object entity) {
        if (entity == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> type = entity.getClass();
        // Incluye tipo para referencia
        map.put("_type", type.getSimpleName());

        // Intenta incluir id si existe getId
        Object idVal = invokeGetter(entity, "getId");
        if (idVal != null) {
            map.put("id", idVal);
        }

        for (Field f : getAllFields(type)) {
            String name = f.getName();
            // evita volver a escribir id si ya lo pusimos
            if ("id".equals(name)) continue;
            Object value = readField(entity, f);
            if (value == null) {
                map.put(name, null);
                continue;
            }
            Class<?> fType = f.getType();
            if (isSimple(fType)) {
                map.put(name, value);
            } else if (isEntity(value.getClass())) {
                Object relId = invokeGetter(value, "getId");
                map.put(name + "Id", relId);
            } else if (value instanceof Collection<?> col) {
                List<Object> ids = new ArrayList<>();
                for (Object elem : col) {
                    if (elem == null) continue;
                    if (isEntity(elem.getClass())) {
                        ids.add(invokeGetter(elem, "getId"));
                    } else if (isSimple(elem.getClass())) {
                        ids.add(elem);
                    } else {
                        // estructura compleja: evita recursión
                        ids.add(String.valueOf(elem));
                    }
                }
                map.put(name + "Ids", ids);
            } else {
                // Evita recursión para objetos complejos
                map.put(name, String.valueOf(value));
            }
        }
        return map;
    }

    private static boolean isEntity(Class<?> c) {
        return c.getPackageName().startsWith(ENTITY_PKG);
    }

    private static boolean isSimple(Class<?> c) {
        return c.isPrimitive() || Number.class.isAssignableFrom(c) || CharSequence.class.isAssignableFrom(c)
                || Boolean.class.isAssignableFrom(c) || Date.class.isAssignableFrom(c)
                || LocalDateTime.class.isAssignableFrom(c) || UUID.class.isAssignableFrom(c)
                || BigDecimal.class.isAssignableFrom(c) || Enum.class.isAssignableFrom(c);
    }

    private static Object invokeGetter(Object target, String getter) {
        try {
            Method m = target.getClass().getMethod(getter);
            return m.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> t = type;
        while (t != null && t != Object.class) {
            Field[] arr = t.getDeclaredFields();
            fields.addAll(Arrays.asList(arr));
            t = t.getSuperclass();
        }
        return fields;
    }

    private static Object readField(Object target, Field f) {
        try {
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            return null;
        }
    }
}
