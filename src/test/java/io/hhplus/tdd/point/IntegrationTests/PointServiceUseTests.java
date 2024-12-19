package io.hhplus.tdd.point.IntegrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.hhplus.tdd.exception.DefaultException;
import io.hhplus.tdd.point.dto.UserPointHistoryResponseDto;
import io.hhplus.tdd.point.dto.UserPointRequestDto;
import io.hhplus.tdd.point.dto.UserPointResponseDto;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.service.PointService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PointServiceUseTests {

  @Autowired
  private PointService pointService;

  @Test
  void 존재하지않는_유저잔액조회테스트() {

    // given
    UserPointRequestDto notExtistUserId = UserPointRequestDto.builder()
                                                             .id(1L)
                                                             .amount(1000L)
                                                             .build();

    // when & then
    assertThrows(DefaultException.class, () -> pointService.searchUserPoint(notExtistUserId));


  }

  @Test
  void 존재하는_유저_잔액조회테스트_충전() {

    // given
    UserPointRequestDto user1 = UserPointRequestDto.builder()
                                                   .id(1L)
                                                   .amount(1000L)
                                                   .build();

    // when

    pointService.chargeUserPoint(user1);

    UserPointResponseDto result = pointService.searchUserPoint(user1);

    // then

    assertThat(result.getId()).isEqualTo(user1.getId());
    assertThat(result.getPoint()).isEqualTo(user1.getAmount());


  }

  @Test
  void 존재하는_유저_잔액조회테스트2_충전후_사용() {

    // given
    UserPointRequestDto chargepoint = UserPointRequestDto.builder()
                                                         .id(1L)
                                                         .amount(10000L)
                                                         .build();

    UserPointRequestDto usepoint = UserPointRequestDto.builder()
                                                      .id(1L)
                                                      .amount(400L)
                                                      .build();

    // when

    pointService.chargeUserPoint(chargepoint);
    pointService.useUserPoint(usepoint);

    UserPointResponseDto result = pointService.searchUserPoint(chargepoint);

    // then

    assertThat(result.getId()).isEqualTo(chargepoint.getId())
                              .isEqualTo(usepoint.getId());
    assertThat(result.getPoint()).isEqualTo(chargepoint.getAmount() - usepoint.getAmount());


  }

  @Test
  void 존재하는_유저_잔액조회테스트2_충전후_초과사용() {

    // given
    UserPointRequestDto chargepoint = UserPointRequestDto.builder()
                                                         .id(1L)
                                                         .amount(1000L)
                                                         .build();

    UserPointRequestDto usepoint = UserPointRequestDto.builder()
                                                      .id(1L)
                                                      .amount(2000L)
                                                      .build();

    // when

    pointService.chargeUserPoint(chargepoint);

    // then

    assertThrows(DefaultException.class, () -> pointService.useUserPoint(usepoint));


  }

  @Test
  void 충전테스트_max값_넘어감() {

    // given
    UserPointRequestDto chargepoint = UserPointRequestDto.builder()
                                                         .id(1L)
                                                         .amount(10000000L)
                                                         .build();

    // when
    // then

    assertThrows(DefaultException.class, () -> pointService.chargeUserPoint(chargepoint));


  }

  @Test
  void 충전테스트_min값_미만() {

    // given
    UserPointRequestDto chargepoint = UserPointRequestDto.builder()
                                                         .id(1L)
                                                         .amount(1L)
                                                         .build();

    // when
    // then

    assertThrows(DefaultException.class, () -> pointService.chargeUserPoint(chargepoint));


  }

  @Test
  void 사용테스트_max값_넘어감() {

    // given
    UserPointRequestDto chargepoint = UserPointRequestDto.builder()
                                                         .id(1L)
                                                         .amount(10000L)
                                                         .build();

    UserPointRequestDto usepoint = UserPointRequestDto.builder()
                                                      .id(1L)
                                                      .amount(100000000L)
                                                      .build();

    // when

    // then

    assertThrows(DefaultException.class, () -> pointService.useUserPoint(usepoint));


  }



  @Test
  void 존재하는_유저_내역조회_테스트2_충전후_사용() {

    // given
    UserPointRequestDto chargepoint = UserPointRequestDto.builder()
                                                         .id(1L)
                                                         .amount(10000L)
                                                         .build();

    UserPointRequestDto usepoint = UserPointRequestDto.builder()
                                                      .id(1L)
                                                      .amount(400L)
                                                      .build();

    // when

    pointService.chargeUserPoint(chargepoint);
    pointService.useUserPoint(usepoint);

    List<UserPointHistoryResponseDto> results = pointService.searchUserPointHistories(chargepoint);

    // then

    assertThat(results.get(0)
                      .getId()).isEqualTo(1L);
    assertThat(results.get(0)
                      .getUserId()).isEqualTo(chargepoint.getId());
    assertThat(results.get(0)
                      .getAmount()).isEqualTo(chargepoint.getAmount());
    assertThat(results.get(0)
                      .getType()).isEqualTo(TransactionType.CHARGE);

    assertThat(results.get(1)
                      .getId()).isEqualTo(2L);
    assertThat(results.get(1)
                      .getUserId()).isEqualTo(usepoint.getId());
    assertThat(results.get(1)
                      .getAmount()).isEqualTo(usepoint.getAmount());
    assertThat(results.get(1)
                      .getType()).isEqualTo(TransactionType.USE);


  }


}
