package io.hhplus.tdd.point.entity;


public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static final long NOT_EXIST_USERID = -1;
    public static final long NOT_EXIST_USERPOINT = -1;

    public static UserPoint empty(long id) {
        return new UserPoint(NOT_EXIST_USERID, NOT_EXIST_USERPOINT, System.currentTimeMillis());
    }

}
