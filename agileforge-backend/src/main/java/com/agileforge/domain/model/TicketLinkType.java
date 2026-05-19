package com.agileforge.domain.model;

public enum TicketLinkType {
    BLOCKS,
    IS_BLOCKED_BY,
    DUPLICATES,
    IS_DUPLICATED_BY,
    RELATES_TO,
    IS_PARENT_OF,
    IS_CHILD_OF;

    public TicketLinkType getInverse() {
        return switch (this) {
            case BLOCKS -> IS_BLOCKED_BY;
            case IS_BLOCKED_BY -> BLOCKS;
            case DUPLICATES -> IS_DUPLICATED_BY;
            case IS_DUPLICATED_BY -> DUPLICATES;
            case RELATES_TO -> RELATES_TO;
            case IS_PARENT_OF -> IS_CHILD_OF;
            case IS_CHILD_OF -> IS_PARENT_OF;
        };
    }

    public boolean isDirectional() {
        return this != RELATES_TO;
    }
}
