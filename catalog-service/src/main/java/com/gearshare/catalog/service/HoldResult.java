package com.gearshare.catalog.service;

public record HoldResult(boolean success, String reason) {

    // NOT named success() -- a record's accessor for the "success" component is
    // already called success() and must return boolean, so a static factory with the
    // same name collides with it and fails to compile.
    public static HoldResult ok() {
        return new HoldResult(true, null);
    }

    public static HoldResult failure(String reason) {
        return new HoldResult(false, reason);
    }
}
