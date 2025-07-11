package com.kernel.global.common.enums;

public enum PostStatus {
    TEMP("임시"),
    REGISTERED("등록"),
    DELETED("삭제");

    private final String label;

    PostStatus(String label) {
        this.label = label;
    }
}
