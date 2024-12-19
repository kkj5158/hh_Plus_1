package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.dto.UserPointHistoryResponseDto;
import io.hhplus.tdd.point.dto.UserPointRequestDto;
import io.hhplus.tdd.point.dto.UserPointResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

  private static final Logger log = LoggerFactory.getLogger(PointController.class);
  private final PointService pointService;

  /**
   * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
   */
  @GetMapping("{id}")
  public UserPointResponseDto point(
      @PathVariable long id
  ) {

    log.info("GET 'point/{id}' request ID: {}", id); // 요청 로그

    UserPointRequestDto userPointRequestDto = UserPointRequestDto
        .builder()
        .id(id)
        .build();

    UserPointResponseDto point = pointService.searchUserPoint(userPointRequestDto);


    log.info("GET 'point/{id}' responese USERPOINT: {}", point.toString()); // 응답 로그

    return point;
  }

  /**
   * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
   */
  @GetMapping("{id}/histories")
  public List<UserPointHistoryResponseDto> history(
      @PathVariable long id
  ) {

    log.info("GET 'point/{id}/histories' request ID: {}", id); // 요청 로그

    UserPointRequestDto userPointRequestDto = UserPointRequestDto
        .builder()
        .id(id)
        .build();

    List<UserPointHistoryResponseDto> results = pointService.searchUserPointHistories(userPointRequestDto);

    log.info("GET 'point/{id}' responese List<PointHistory>: {}", results.toString()); // 응답 로그


    return results;
  }

  /**
   * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
   */
  @PatchMapping("{id}/charge")
  public UserPointResponseDto charge(
      @PathVariable long id,
      @RequestBody long amount
  ) {

    log.info("Patch 'point/{id}/charge' request ID: {}", id); // 요청 로그
    log.info("Patch 'point/{id}/charge' request amount: {}", amount); // 요청 로그

    UserPointRequestDto userPointRequestDto = UserPointRequestDto.builder().
        id(id)
        .amount(amount)
        .build();

    UserPointResponseDto balancepoint =  pointService.chargeUserPoint(userPointRequestDto);

    log.info("Patch 'point/{id}/charge' responese UserPoint(balance): {}", balancepoint.toString()); // 응답 로그


    return balancepoint;
  }
  /**
   * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
   */
  @PatchMapping("{id}/use")
  public UserPointResponseDto use(
      @PathVariable long id,
      @RequestBody long amount
  ) {

    log.info("Patch 'point/{id}/use' request ID: {}", id); // 요청 로그
    log.info("Patch 'point/{id}/use' request amount: {}", amount); // 요청 로그

    UserPointRequestDto userPointRequestDto = UserPointRequestDto.builder().
        id(id)
        .amount(amount)
        .build();

    UserPointResponseDto balancepoint = pointService.useUserPoint(userPointRequestDto);

    log.info("Patch 'point/{id}/charge' responese UserPoint(balance): {}", balancepoint.toString()); // 응답 로그

    return balancepoint;

  }
}
