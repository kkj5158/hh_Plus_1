package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.UserPointHistoryResponseDto;
import io.hhplus.tdd.point.dto.UserPointRequestDto;
import io.hhplus.tdd.point.dto.UserPointResponseDto;
import io.hhplus.tdd.point.record.PointHistory;
import io.hhplus.tdd.point.record.UserPoint;
import java.util.List;

public interface PointService {

  public UserPointResponseDto searchUserPoint(UserPointRequestDto userPointRequestDto);

  public List<UserPointHistoryResponseDto>  searchUserPointHistories(UserPointRequestDto userPointRequestDto);

  public UserPointResponseDto chargeUserPoint(UserPointRequestDto userPointRequestDto);

  public  UserPointResponseDto useUserPoint(UserPointRequestDto userPointRequestDto);





}
