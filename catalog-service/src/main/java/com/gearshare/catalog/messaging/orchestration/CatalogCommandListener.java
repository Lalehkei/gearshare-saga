package com.gearshare.catalog.messaging.orchestration;

import com.gearshare.catalog.service.CatalogSagaService;
import com.gearshare.catalog.service.HoldResult;
import com.gearshare.events.Topics;
import com.gearshare.events.commands.CommandReply;
import com.gearshare.events.commands.HoldAvailabilityCommand;
import com.gearshare.events.commands.ReleaseAvailabilityCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * ORCHESTRATION MODE ONLY (active when SPRING_PROFILES_ACTIVE=orchestration).
 * <p>
 * Does nothing but translate explicit commands from orchestrator-service into calls on
 * {@link CatalogSagaService}, and translate the result back into a {@link CommandReply}.
 * Compare this to {@link com.gearshare.catalog.messaging.choreography.CatalogEventListener}
 * -- same underlying service, completely different way of being triggered.
 */
@Component
@Profile("orchestration")
@RequiredArgsConstructor
@Slf4j
public class CatalogCommandListener {

    private final CatalogSagaService catalogSagaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = Topics.CATALOG_HOLD_CMD, groupId = "catalog-service")
    public void onHold(HoldAvailabilityCommand command) {
        log.info("[{}] received HoldAvailabilityCommand", command.rentalId());
        HoldResult result = catalogSagaService.holdAvailability(
                command.rentalId(), command.itemId(), command.startDate(), command.endDate());

        CommandReply reply = result.success()
                ? CommandReply.ok(command.rentalId())
                : CommandReply.fail(command.rentalId(), result.reason());
        kafkaTemplate.send(Topics.CATALOG_REPLY, command.rentalId(), reply);
    }

    @KafkaListener(topics = Topics.CATALOG_RELEASE_CMD, groupId = "catalog-service")
    public void onRelease(ReleaseAvailabilityCommand command) {
        log.info("[{}] received ReleaseAvailabilityCommand (compensation)", command.rentalId());
        catalogSagaService.releaseAvailability(command.rentalId());
        // Compensating commands don't get a reply: the orchestrator has already decided
        // the saga is cancelled and is unwinding unconditionally, not waiting on us.
    }
}
