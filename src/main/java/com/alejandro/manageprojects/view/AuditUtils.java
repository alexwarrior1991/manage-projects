package com.alejandro.manageprojects.view;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Utilidades para consultar historial de auditoría (Hibernate Envers).
 */
@Component
public class AuditUtils {

    @PersistenceContext
    private EntityManager entityManager;

    private AuditReader auditReader() {
        return AuditReaderFactory.get(entityManager);
    }

    /**
     * Devuelve los números de revisión existentes para una entidad concreta.
     */
    public <T, ID> List<Number> getRevisionNumbers(Class<T> entityClass, ID id) {
        if (Objects.isNull(id)) return Collections.emptyList();
        return new ArrayList<>(auditReader().getRevisions(entityClass, id));
    }

    /**
     * Obtiene todas las versiones (snapshots) de una entidad por su id.
     */
    public <T, ID> List<T> getRevisions(Class<T> entityClass, ID id) {
        List<Number> revs = getRevisionNumbers(entityClass, id);
        List<T> result = new ArrayList<>(revs.size());
        for (Number rev : revs) {
            result.add(auditReader().find(entityClass, id, rev));
        }
        return result;
    }

    /**
     * Obtiene la versión de una entidad en una revisión concreta.
     */
    public <T, ID> T getAtRevision(Class<T> entityClass, ID id, Number revision) {
        return auditReader().find(entityClass, id, revision);
    }

    /**
     * Obtiene la última versión conocida de la entidad basada en auditoría.
     */
    public <T, ID> T getLastRevision(Class<T> entityClass, ID id) {
        List<Number> revs = getRevisionNumbers(entityClass, id);
        if (revs.isEmpty()) return null;
        Number last = revs.get(revs.size() - 1);
        return getAtRevision(entityClass, id, last);
    }

    // ------------------------
    // Métodos extendidos por fecha/rangos
    // ------------------------

    /**
     * Obtiene el estado de la entidad en una fecha/hora concreta.
     */
    public <T, ID> T getAtDate(Class<T> entityClass, ID id, LocalDateTime atDateTime) {
        if (atDateTime == null || id == null) return null;
        Date date = toDate(atDateTime);
        return auditReader().find(entityClass, id, date);
    }

    /**
     * Devuelve los números de revisión cuya marca de tiempo cae entre las fechas indicadas (inclusive).
     */
    public <T, ID> List<Number> getRevisionNumbersBetween(Class<T> entityClass, ID id,
                                                          LocalDateTime from, LocalDateTime to) {
        if (id == null) return Collections.emptyList();
        Long fromMs = from != null ? toMillis(from) : null;
        Long toMs = to != null ? toMillis(to) : null;

        AuditQuery query = auditReader().createQuery().forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id));
        if (fromMs != null) {
            query.add(AuditEntity.revisionProperty("timestamp").ge(fromMs));
        }
        if (toMs != null) {
            query.add(AuditEntity.revisionProperty("timestamp").le(toMs));
        }
        // Orden por revision ascendente
        query.addOrder(AuditEntity.revisionNumber().asc());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = (List<Object[]>) query.getResultList();
        // forRevisionsOfEntity(false, true) devuelve [entity, revisionEntity, revisionType]
        List<Number> revisions = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            Number revNumber = auditReader().getRevisionNumberForDate(
                    auditReader().getRevisionDate((Number) auditReader().getRevisionNumberForDate(
                            ((org.hibernate.envers.DefaultRevisionEntity) row[1]).getRevisionDate())));
            // La forma anterior es redundante; mejor obtenerla del propio objeto de revisión si es DefaultRevisionEntity
        }

        // Alternativa robusta: extraer desde la segunda columna (revision entity) vía AuditReader utilidades
        // Como el tipo de revision entity depende de la configuración, usaremos AuditReader#getRevisionNumberForDate
        // apoyándonos en la fecha extraída por reflexión segura

        revisions.clear();
        for (Object[] row : rows) {
            Object revEntity = row[1];
            Number revNumber = extractRevisionNumber(revEntity);
            if (revNumber != null) revisions.add(revNumber);
        }
        return revisions;
    }

    /**
     * Obtiene las versiones de la entidad cuyas revisiones caen entre fechas.
     */
    public <T, ID> List<T> getRevisionsBetween(Class<T> entityClass, ID id,
                                               LocalDateTime from, LocalDateTime to) {
        List<Number> revs = getRevisionNumbersBetween(entityClass, id, from, to);
        List<T> result = new ArrayList<>(revs.size());
        for (Number rev : revs) {
            result.add(auditReader().find(entityClass, id, rev));
        }
        return result;
    }

    /**
     * Devuelve un mapa ordenado revisionNumber -> fecha/hora de la revisión (zona del sistema).
     */
    public Map<Number, LocalDateTime> getRevisionTimestamps(Class<?> entityClass, Object id) {
        List<Number> revs = getRevisionNumbers((Class<Object>) entityClass, id);
        Map<Number, LocalDateTime> map = new LinkedHashMap<>();
        for (Number rev : revs) {
            Date d = auditReader().getRevisionDate(rev);
            map.put(rev, toLocalDateTime(d));
        }
        return map;
    }

    /**
     * Fecha/hora de la primera revisión donde la entidad aparece.
     */
    public LocalDateTime getFirstChangeDate(Class<?> entityClass, Object id) {
        List<Number> revs = getRevisionNumbers((Class<Object>) entityClass, id);
        if (revs.isEmpty()) return null;
        return toLocalDateTime(auditReader().getRevisionDate(revs.get(0)));
        
    }

    /**
     * Fecha/hora de la última revisión registrada de la entidad.
     */
    public LocalDateTime getLastChangeDate(Class<?> entityClass, Object id) {
        List<Number> revs = getRevisionNumbers((Class<Object>) entityClass, id);
        if (revs.isEmpty()) return null;
        return toLocalDateTime(auditReader().getRevisionDate(revs.get(revs.size() - 1)));
    }

    // --------- helpers ---------
    private static Date toDate(LocalDateTime ldt) {
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    private static long toMillis(LocalDateTime ldt) {
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Number extractRevisionNumber(Object revisionEntity) {
        // Revisión por defecto de Envers implementa getId() o getRevisionNumber dependiendo de versión
        try {
            // Intentar método getId()
            var m = revisionEntity.getClass().getMethod("getId");
            Object val = m.invoke(revisionEntity);
            if (val instanceof Number) return (Number) val;
        } catch (Exception ignored) { }
        try {
            var m = revisionEntity.getClass().getMethod("getRevision");
            Object val = m.invoke(revisionEntity);
            if (val instanceof Number) return (Number) val;
        } catch (Exception ignored) { }
        try {
            var m = revisionEntity.getClass().getMethod("getTimestamp");
            Object ts = m.invoke(revisionEntity);
            if (ts instanceof Number numTs) {
                // Mapear timestamp a número de revisión usando AuditReader
                Date date = new Date(numTs.longValue());
                return auditReader().getRevisionNumberForDate(date);
            }
        } catch (Exception ignored) { }
        return null;
    }
}
