package com.gearshare.events;

/**
 * Single source of truth for every Kafka topic in the system.
 * <p>
 * One topic carries exactly one message type -- no polymorphic payloads to untangle.
 * That trade-off costs us more topics than a "generic command bus" design would need,
 * but it means every listener method has one concrete, obvious type, which is worth it
 * for a project meant to be read.
 * <p>
 * Topics are grouped by which saga they belong to and which coordination style uses them:
 * <ul>
 *   <li><b>Shared</b>      - used by both orchestration and choreography</li>
 *   <li><b>Orchestration</b> - explicit command/reply pairs, one per participant, used only
 *                              when the orchestrator-service is coordinating</li>
 *   <li><b>Choreography</b>  - domain events that participants react to directly, used only
 *                              when no orchestrator is running</li>
 *   <li><b>Fulfillment</b> - the second, simpler saga that runs after a rental is confirmed
 *                            (pickup -&gt; return -&gt; capture/damage), which is choreographed
 *                            in both modes since it's a short linear chain</li>
 * </ul>
 */
public final class Topics {

    private Topics() {
    }

    // ---- Shared: saga #1 entry point and terminal outcome ------------------------------
    public static final String RENTAL_REQUESTED = "rental.requested";
    public static final String RENTAL_CONFIRMED = "rental.confirmed";
    public static final String RENTAL_CANCELLED = "rental.cancelled";

    // ---- Orchestration: commands issued by orchestrator-service, replies sent back -----
    public static final String CATALOG_HOLD_CMD = "catalog.commands.hold";
    public static final String CATALOG_RELEASE_CMD = "catalog.commands.release";
    public static final String CATALOG_REPLY = "catalog.replies";

    public static final String APPROVAL_REQUEST_CMD = "approval.commands.request";
    public static final String APPROVAL_CANCEL_CMD = "approval.commands.cancel";
    public static final String APPROVAL_REPLY = "approval.replies";

    public static final String PAYMENT_AUTHORIZE_CMD = "payment.commands.authorize";
    public static final String PAYMENT_RELEASE_CMD = "payment.commands.release";
    public static final String PAYMENT_REPLY = "payment.replies";

    // ---- Choreography: domain events participants react to directly --------------------
    public static final String AVAILABILITY_HELD = "availability.held";
    public static final String AVAILABILITY_REJECTED = "availability.rejected";

    public static final String APPROVAL_GRANTED = "approval.granted";
    public static final String APPROVAL_REJECTED = "approval.rejected";
    public static final String APPROVAL_TIMEDOUT = "approval.timedout";

    public static final String PAYMENT_AUTHORIZED = "payment.authorized";
    public static final String PAYMENT_AUTHORIZATION_FAILED = "payment.authorization-failed";

    // ---- Fulfillment saga (post-confirmation: pickup / return / capture) ----------------
    public static final String PICKUP_CONFIRMED = "pickup.confirmed";
    public static final String RETURN_CONFIRMED = "return.confirmed";
    public static final String DAMAGE_CLAIM_FILED = "damage.claim.filed";
    public static final String PAYMENT_CAPTURED = "payment.captured";
    public static final String PAYMENT_PARTIALLY_CAPTURED = "payment.partially-captured";
}
