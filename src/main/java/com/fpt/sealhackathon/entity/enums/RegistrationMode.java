package com.fpt.sealhackathon.entity.enums;

public enum RegistrationMode {
    NEW("new"),
    RESUBMITTED("resubmitted"),
    IMPORTED("imported"),
    MANUAL("manual");

    private final String dbValue;

    RegistrationMode(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }
}
