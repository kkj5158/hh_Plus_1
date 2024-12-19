package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.UserPointHistoryResponseDto;
import io.hhplus.tdd.point.dto.UserPointRequestDto;
import io.hhplus.tdd.point.dto.UserPointResponseDto;
import java.util.List;

public interface PointService {

   UserPointResponseDto searchUserPoint(UserPointRequestDto userPointRequestDto);

   List<UserPointHistoryResponseDto>  searchUserPointHistories(UserPointRequestDto userPointRequestDto);

   UserPointResponseDto chargeUserPoint(UserPointRequestDto userPointRequestDto);

    UserPointResponseDto useUserPoint(UserPointRequestDto userPointRequestDto);





}
