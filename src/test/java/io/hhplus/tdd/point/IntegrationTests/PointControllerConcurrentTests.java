package io.hhplus.tdd.point.IntegrationTests;

import io.hhplus.tdd.point.dto.UserPointResponseDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) // 고정 포트 사용
public class PointControllerConcurrentTests {

  private static final String BASE_URL = "http://localhost:8080/point";
  private static final int THREAD_COUNT = 32;


  @Autowired
  private RestTemplate restTemplate;

  @Test
  void 유저1명_100L_쓰레드갯수만큼_충전_동시성_포인트_충전_테스트() throws InterruptedException {
    // given
    long userId = 1L;
    long chargeAmount = 100L;

    // 스레드 풀과 동기화 도구
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

    // when
    for (int i = 0; i < THREAD_COUNT; i++) {

      executorService.submit(() -> {
        try {
          String url = BASE_URL + "/" + userId + "/charge";
          restTemplate.put(url, chargeAmount, Void.class);
        }
        catch (Exception e) {
          e.printStackTrace(); // 예외를 로그에 출력하여 문제 원인을 확인
        }
        finally {
          latch.countDown();
        }
      });
    }

    latch.await(); // 모든 작업이 끝날 때까지 대기

    // then
    String getPointUrl = BASE_URL + "/" + userId;
    ResponseEntity<UserPointResponseDto> response = restTemplate.getForEntity(getPointUrl, UserPointResponseDto.class);

    assertThat(response.getBody().getPoint()).isEqualTo(THREAD_COUNT * chargeAmount);
  }

  @Test
  void 동시성_포인트_사용_테스트() throws InterruptedException {
    // given

    long useUserId = 1L;
    long useUserAmount = 10L;

    long chargeUserId = 1L;
    long chargeUserAmount = useUserAmount * THREAD_COUNT;


    // 스레드 풀과 동기화 도구
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

    // when
    // 충전
    String chargeUrl = BASE_URL + "/" + chargeUserId + "/charge";
    restTemplate.put(chargeUrl, chargeUserAmount, Void.class);


    for (int i = 0; i < THREAD_COUNT; i++) {
      executorService.submit(() -> {
        try {
          String url = BASE_URL + "/" + useUserId + "/use";
          restTemplate.put(url, useUserAmount, Void.class);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await(); // 모든 작업이 끝날 때까지 대기

    // then
    String getPointUrl = BASE_URL + "/" + useUserId;
    ResponseEntity<UserPointResponseDto> response = restTemplate.getForEntity(getPointUrl, UserPointResponseDto.class);

    assertThat(response.getBody().getPoint()).isEqualTo(0L); // 초기값 충전 후 사용한 경우 0이 예상
  }

  @Test
  void 동시성_포인트_히스토리_테스트() throws InterruptedException {
    // given
    long userId = 1L;


    // 스레드 풀과 동기화 도구
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

    // when
    for (int i = 0; i < THREAD_COUNT; i++) {
      executorService.submit(() -> {
        try {
          String url = BASE_URL + "/" + userId + "/charge";
          restTemplate.put(url, 10L, Void.class);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await(); // 모든 작업이 끝날 때까지 대기

    // then
    String getHistoriesUrl = BASE_URL + "/" + userId + "/histories";
    ResponseEntity<List> response = restTemplate.getForEntity(getHistoriesUrl, List.class);

    assertThat(response.getBody().size()).isEqualTo(THREAD_COUNT); // 요청 수만큼 기록이 있어야 함
  }

}
