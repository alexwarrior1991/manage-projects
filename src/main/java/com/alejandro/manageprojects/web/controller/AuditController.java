package com.alejandro.manageprojects.web.controller;

import com.alejandro.manageprojects.domain.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    // GET /api/v1/audit/{entity}/{id}/revisions
    @GetMapping(path = "/{entity}/{id}/revisions")
    public ResponseEntity<List<Number>> getRevisionNumbers(@PathVariable String entity,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(auditService.getRevisionNumbers(entity, id));
    }

    // GET /api/v1/audit/{entity}/{id}/timestamps
    @GetMapping(path = "/{entity}/{id}/timestamps")
    public ResponseEntity<Map<Number, LocalDateTime>> getRevisionTimestamps(@PathVariable String entity,
                                                                            @PathVariable Long id) {
        return ResponseEntity.ok(auditService.getRevisionTimestamps(entity, id));
    }

    // GET /api/v1/audit/{entity}/{id}/at-revision/{rev}
    @GetMapping(path = "/{entity}/{id}/at-revision/{rev}")
    public ResponseEntity<Map<String, Object>> getAtRevision(@PathVariable String entity,
                                                             @PathVariable Long id,
                                                             @PathVariable Number rev) {
        return ResponseEntity.ok(auditService.getAtRevision(entity, id, rev));
    }

    // GET /api/v1/audit/{entity}/{id}/at-date?dateTime=...
    @GetMapping(path = "/{entity}/{id}/at-date")
    public ResponseEntity<Map<String, Object>> getAtDate(@PathVariable String entity,
                                                         @PathVariable Long id,
                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        return ResponseEntity.ok(auditService.getAtDate(entity, id, dateTime));
    }

    // GET /api/v1/audit/{entity}/{id}/between?from=...&to=...
    @GetMapping(path = "/{entity}/{id}/between")
    public ResponseEntity<List<Map<String, Object>>> getBetween(@PathVariable String entity,
                                                                @PathVariable Long id,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(auditService.getBetween(entity, id, from, to));
    }
}
