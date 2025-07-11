package com.kernel.sharedDomain.common.enums;

public enum MatchStatus {
    PENDING("대기"),
    MATCHED("매칭 완료"),
    REJECTED("매니저 거절"),
    RESERVATION_CANCELED("예약 취소");

    private final String label;

    MatchStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
