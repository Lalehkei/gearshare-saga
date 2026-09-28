package com.gearshare.orchestrator.web;

import com.gearshare.orchestrator.domain.SagaInstance;
import com.gearshare.orchestrator.domain.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only visibility into the orchestrator's state machine -- there's no equivalent
 * of this in choreography mode, because there's no single place choreography keeps
 * this view. That contrast is worth pointing out to yourself while you're running both
 * modes side by side.
 */
@RestController
@RequestMapping("/api/sagas")
@RequiredArgsConstructor
public class SagaController {

    private final SagaInstanceRepository repository;

    @GetMapping("/{rentalId}")
    public ResponseEntity<SagaInstance> get(@PathVariable String rentalId) {
        return repository.findById(rentalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<SagaInstance> list() {
        return repository.findAll();
    }
}
