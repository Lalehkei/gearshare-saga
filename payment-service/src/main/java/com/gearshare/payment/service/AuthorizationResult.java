package com.gearshare.payment.service;

public record AuthorizationResult(boolean success, String reason) {

    // NOT named success() -- see HoldResult's comment: a record's own accessor for the
    // "success" component would collide with a static factory of the same name.
    public static AuthorizationResult ok() {
        return new AuthorizationResult(true, null);
    }

    public static AuthorizationResult failure(String reason) {
        return new AuthorizationResult(false, reason);
    }
}
