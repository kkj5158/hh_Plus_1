package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.DefaultException;
import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.dto.UserPointHistoryResponseDto;
import io.hhplus.tdd.point.dto.UserPointRequestDto;
import io.hhplus.tdd.point.dto.UserPointResponseDto;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {


  private static final Logger log = LoggerFactory.getLogger(PointController.class);
  private static final long MAX_USER_POINT = 100000L;
  private static final long MIN_USER_POINT = 10L;
  private final PointHistoryTable pointHistoryTable;
  private final UserPointTable userPointTable;

  public static void validatAmount(long amount) {
    if (amount > MAX_USER_POINT) {
      throw new DefaultException(HttpStatus.BAD_REQUEST, "입력된 amount값이 최대값 100000 초과입니다.");
    } else if (amount < MIN_USER_POINT) {
      throw new DefaultException(HttpStatus.BAD_REQUEST, "입력된 amount값이 최대값 10 미만입니다.");
    }

  }


  public static void validateUserId(long userId) {
    if (userId == UserPoint.NOT_EXIST_USERID) {
      throw new DefaultException(HttpStatus.NOT_FOUND, "존재하지 않는 userid 입니다.");
    }

  }

  public static boolean isExistUserId(long userId) {
    return userId != UserPoint.NOT_EXIST_USERID;
  }

  @Override
  public UserPointResponseDto searchUserPoint(UserPointRequestDto userPointRequestDto) {

    log.info("PointService.searchUserPoint requestDto : {}", userPointRequestDto.toString()); // 요청 로그

    UserPoint userPoint = userPointTable.selectById(userPointRequestDto.getId());

    validateUserId(userPoint.id());

    UserPointResponseDto userPointResponseDto = UserPointResponseDto.builder()
                                                                    .id(userPoint.id())
                                                                    .point(userPoint.point())
                                                                    .updateMillis(userPoint.updateMillis())
                                                                    .build();

    log.info("PointService.searchUserPoint responseDto : {}", userPointResponseDto.toString()); // 응답 로그

    return userPointResponseDto;
  }


  @Override
  public List<UserPointHistoryResponseDto> searchUserPointHistories(UserPointRequestDto userPointRequestDto) {

    log.info("PointService.searchUserPointHistories requestDto : {}", userPointRequestDto.toString()); // 요청 로그

    long id = userPointRequestDto.getId();

    UserPoint userPoint = userPointTable.selectById(id);

    validateUserId(userPoint.id());

    List<UserPointHistoryResponseDto> responseDtos = pointHistoryTable.selectAllByUserId(id)
                                                                      .stream()
                                                                      .map(entity -> UserPointHistoryResponseDto.builder()
                                                                                                                .id(entity.id())
                                                                                                                .userId(entity.userId())
                                                                                                                .amount(entity.amount())
                                                                                                                .type(entity.type())
                                                                                                                .updateMillis(entity.updateMillis())
                                                                                                                .build())
                                                                      .collect(Collectors.toList());

    log.info("PointService.searchUserPointHistories responseDto : {}", responseDtos); // 응답 로그

    return responseDtos;


  }

  @Override
  public UserPointResponseDto chargeUserPoint(UserPointRequestDto userPointRequestDto) {

    log.info("PointService.userPointRequestDto requestDto : {}", userPointRequestDto.toString()); // 요청 로그

    long id = userPointRequestDto.getId();
    long amount = userPointRequestDto.getAmount();

    validatAmount(amount);

    UserPoint beforeChargeUserPoint = userPointTable.selectById(id);
    UserPoint afterChargeuserPoint;

    if (isExistUserId(beforeChargeUserPoint.id())) {
      // 기존 유저인 경우
      afterChargeuserPoint = userPointTable.insertOrUpdate(id, beforeChargeUserPoint.point() + amount);
    } else {
      // 신규 유저인 경우
      afterChargeuserPoint = userPointTable.insertOrUpdate(id, userPointRequestDto.getAmount());
    }

    pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

    //충전후 남은 잔액을 나타내는 userPoint 반환
    UserPointResponseDto userPointResponseDto = UserPointResponseDto.builder()
                                                                    .id(afterChargeuserPoint.id())
                                                                    .point(afterChargeuserPoint.point())
                                                                    .updateMillis(afterChargeuserPoint.updateMillis())
                                                                    .build();

    log.info("PointService.chargeUserPoint responseDto : {}", userPointResponseDto.toString()); // 응답 로그

    return userPointResponseDto;

  }

  @Override
  public UserPointResponseDto useUserPoint(UserPointRequestDto userPointRequestDto) {

    log.info("PointService.useUserPoint requestDto : {}", userPointRequestDto.toString()); // 요청 로그

    long id = userPointRequestDto.getId();
    long amount = userPointRequestDto.getAmount();

    validatAmount(amount);

    UserPoint berforeuserUserPoint = userPointTable.selectById(userPointRequestDto.getId());
    UserPoint afteruserUserPoint;

    validateUserId(berforeuserUserPoint.id());

    if (berforeuserUserPoint.point() - amount < 0) {
      throw new DefaultException(HttpStatus.BAD_REQUEST, "충전되어있는 포인트 이상 사용할 수 없습니다.");
    }

    afteruserUserPoint = userPointTable.insertOrUpdate(id, berforeuserUserPoint.point() - amount);

    pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
    // 사용하고 남은 금액을 나타내는 userPoint 반환
    UserPointResponseDto userPointResponseDto = UserPointResponseDto.builder()
                                                                    .id(afteruserUserPoint.id())
                                                                    .point(afteruserUserPoint.point())
                                                                    .updateMillis(afteruserUserPoint.updateMillis())
                                                                    .build();

    log.info("PointService.useUserPoint responseDto : {}", userPointResponseDto.toString()); // 응답 로그

    return userPointResponseDto;

  }

}
