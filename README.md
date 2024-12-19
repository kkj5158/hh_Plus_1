# 1. 동시성 제어를 위한 코드의 작성 및 분석

### 1. 동시성 제어를 위한 테스트 코드의 작성

처음 , 동시성 제어를 위해서 테스트코드를 작성할 때, 동시다발적으로 여러 요청이 들어와야 하므로 멀티쓰레드 구조를 통한 테스트 코드 작성을 떠올린 후 다음과 같은 코드를 작성하였습니다. 

```java
    
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
    
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
```

- restTemplate을 사용하여서 http 요청을 보냄
- ExecutorService 와 CountDownLatch 를 활용하여서 멀티쓰레드 구조 활용
    - `ExecutorService`를 통해 스레드 풀을 관리.
    - `CountDownLatch`로 모든 스레드가 완료될 때까지 대기.

테스트 케이스는 다음과 같이 작성하였습니다. 

1-1 : 여러 스레드가 동시에 한 사용자의 포인트를 충전했을 때, 최종 포인트 값이 예상대로 계산되는지 확인.

1-2 : 여러 스레드가 한 사용자의 포인트를 동시에 사용했을 때, 포인트가 정확히 차감되는지 확인.

1-3 : 동시성 상황에서 포인트 충전 이력 로그가 정확히 기록되는지 확인.

```java
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

```

### 2. 동시성 제어가 대비되어있지 않은 서비스 로직에서의 테스트 결과 → 문제발생

1-1 : 여러 스레드가 동시에 한 사용자의 포인트를 충전했을 때, 최종 포인트 값이 예상대로 계산되는지 확인.

1-2 : 여러 스레드가 한 사용자의 포인트를 동시에 사용했을 때, 포인트가 정확히 차감되는지 확인.

두가지 상황에서 문제가 발생했습니다.최종포인트값이 예상과는 다르거나. 포인트가 정확히 차감되지 않는 문제 발생 

서비스계층에서의 동시성 제어를 위한 로직을 보강해야겠다는 판단이 들었습니다. 

### 3. 서비스 계층 로직 변경

```java
  Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
    lock.lock();

    try {
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
    finally {
      lock.unlock();
      userLocks.remove(id);
    }

```

서비스단에 다음과 같이 락을 이용하여 보완하였습니다. 

```java
Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
lock.lock();

```

- 사용자 ID(`id`)를 키로 하여 `Lock` 객체를 관리합니다.
- `computeIfAbsent`는 해당 키에 `Lock`이 없으면 새로 생성하고 반환합니다.
- `lock.lock()`을 호출하여 해당 사용자 ID에 대한 작업을 다른 스레드가 실행하지 못하도록 보호합니다.

```java
finally {
    lock.unlock();
    userLocks.remove(id);
}

```

- 작업이 완료되면 `lock.unlock()`를 호출하여 다른 스레드가 동일한 사용자 ID에 대해 작업할 수 있도록 해제합니다.

### 4. 서비스계층 로직 변경이후의 테스트


다음과 같이 테스트에 성공했음을 확인했습니다. 

![adf](https://github.com/user-attachments/assets/6f94d2fb-c1f5-4ba3-ab0b-da90794fee3f)

# 2. 동시성제어에 대한 개념과 이해

### 1. 동시성 제어 정의

**동시성 제어(Concurrency Control)란,** 여러 사용자나 프로세스가 데이터를 공유할 때 발생할 수 있는 문제를 해결하기 위해, 데이터베이스 시스템이 트랜잭션을 순서대로 실행하는 것이 아니라, 트랜잭션이 동시에 실행될 수 있도록 허용하면서도 데이터의 일관성과 무결성을 유지할 수 있도록 하는 기술이며, 데이터베이스 시스템에서 매우 중요한 개념 중 하나이다.

### 2. 동시성 제어 목적

- 여러 사용자가 DB에 접근하더라도 데이터의 일관성을 보장하고 데이터의 무결성을 유지
- 위를 만족하며 데이터베이스 시스템의 성능과 효율성을 유지하는 것
1. **분실된 갱신(Lost Update)**
    - 두개의 트랜잭션이 같은 데이터를 갱신하는 작업을 진행하게 되면서 하나의 작업이 진행되지 않는 경우
2. **모순성(Inconsistency)**
    - 두개의 트랜잭션이 같은 데이터를 동시에 갱신하게되어 사용자가 원하는 결과와 일치하지 않은 상태가 되는 경우
3. **연쇄복귀(Cascading Rollback)**
    - 두개의 트랜잭션이 같은 데이터를 갱신하는 작업을 진행하는 과정에서 하나의 트랜잭션이 실패하면 원자성에 의해 두 트랜잭션 모두 복귀하는 경우
4. **비완료 의존성(Uncommitted Dependency)**
    - 한개의 트랜잭션이 실패하였을때, 이 트랜재션이 회복하기전에 다른 트랜잭션이 실패한 수행 결과를 참조하는 경우

### 3. 동시성 제어 방법

#### 3-1. **Locking**

##### Locking이란?

- Locking은 공유 자원에 대한 동시 액세스를 제어하는 전통적인 방법이다.
- 단일 스레드 또는 단일 프로세스에서만 동작하며, 한 번에 하나의 스레드만 공유 자원에 액세스할 수 있다.
- 신뢰성과 안전성이 높으며, 어떤 수준의 locking을 적용하느냐에 따라 교착 상태나 경쟁 조건과 같은 문제를 방지할 수 있다.
- 하지만 동시성 처리 속도가 저하될 수 있고, 대기 시간이 발생할 수 있다.
- 기본적으로 lock 연산과 unlock 연산을 사용한다.

##### Locking의 종류

- **공유 잠금 (shared lock/s-lock): 데이터를 읽을 때 사용하는 락**
    - 공유잠금을 설정한 트랜잭션은 데이터 항목에 대해 **읽기 연산(read)만 가능**하다.
        - T1에서 x에 대해 S-lock을 설정했다면, T1은 read(x) 연산만 가능하다.
    - 하나의 데이터 항목에 대해 **여러 개의 공유잠금이(S-lock) 가능**하다.
        - T1에서 x에 대해 S-lock을 설정한 경우, 동시에 T2에서도 x에 대해 S-lock을 설정할 수 있다.
    - **다른 트랜잭션도 읽기 연산(read) 만을 실행할 수 있다**.
        - T1에서 x에 대해 S-lock을 설정했다면, T2에서도 T1이 S-lock(x)을 실행하는 동안 read(x) 연산만 가능하다.
- **배타 잠금 (exclusive lock/x-lock): 데이터를 변경할 때 사용하는 락**
    - 배타잠금을 설정한 트랜잭션은 데이터 항목에 대해서 **읽기 연산(read)과 쓰기 연산(write) 모두 가능**하다.
        - T1에서 x에 대해 S-lock을 설정했다면, T1은 read(x) 연산과 write(x) 연산 모두 가능하다.
    - 하나의 데이터 항목에 대해서는 **하나의 배타잠금(X-lock)만 가능**하다.
    - **동시에 여러 개의 배타잠금은 불가능**하다.
        - T1에서 x에 대해 X-lock을 설정했다면, T1에서 unlock(x)를 하기 전까지 T2에서 x에 대해 X-lock을 설정할 수 없다.
    - **다른 트랜잭션은 읽기 연산(read)와 쓰기 연산(write) 모두 불가능**하다.
        - T1에서 x에 대해 X-lock을 설정했다면, T2에서는 T1에서 unlock(x)를 하기 전까지 read(x), write(x) 연산이 모두 불가능하다.
- **추가) 교착상태 (deadlock)**
    - 모든 transaction이 대기 상태에 들어가 아무런 진행이 일어나지 않는 상태를 교착상태라고 한다. 교착상태에 빠지면 외부에서 강제로 트랜잭션을 중단하거나 잠금을 해제하지 않는 이상 무한정 대기 상태로 남게 된다.

---

### **Locking을 활용한 동시성 제어 기법**

- **낙관적 락 (optimistic lock)**
    - 충돌이 발생할 가능성이 낮은 경우 사용되는 동시성 제어 기법이다.
    - 충돌이 발생하면 재시도 또는 병합을 통해 충돌을 해결한다.
    - 실제로 lock을 사용하지 않고 **version**을 이용함으로서 정합성을 맞추는 방법이다. 데이터를 읽을 때 lock을 사용하지 않고, 업데이트 시 **내가 읽은 version이 맞는지 충돌 여부를 확인**하여 처리한다.
    - **즉, 자원에 lock을 직접 걸어서 선점하지 않고, 동시성 문제가 실제로 발생하면 그때가서 처리하는 방식이다.**
- **비관적 락 (pessimistic lock)**
    - 충돌이 발생할 가능성이 높은 경우 사용되는 동시성 제어 기법이다.
    - 데이터를 읽거나 수정하기 전에 lock을 획득하여 다른 사용자의 액세스를 차단하고, lock을 가진 스레드만 접근하도록 제어한다.
    - 데이터에 대한 배타적인 액세스 권한을 보장하여 충돌을 방지한다.
    - 실제로 데이터에 lock을 걸어서 정합성을 맞추는 방법으로, 자원 요청에 따른 동시성 문제가 발생할 것이라고 예상하고 lock을 걸어버리는 방법이다.
    - **즉, 트랜젝션이 시작할 때 s-lock이나 x-lock을 실제로 걸고 시작한다.**
- **낙관적 락 vs. 비관적 락**

|  | 낙관적 락 | 비관적 락 |
| --- | --- | --- |
| 장점 | 트랜젝션을 필요로 하지 않고, 별도의 lock을 사용하지 않으므로 성능적으로 좋다. | 동시성 문제가 빈번하게 일어난다면 rollback의 횟수를 줄일 수 있기 때문에 성능적으로 좋다. |
| 단점 | 동시성 문제가 빈번하게 일어나면 계속 rollback 처리를 해주어야 하며, 업데이트가 실패했을 때 재시도 로직도 개발자가 직접 작성해야 한다. | 모든 트랜젝션에 lock을 사용하기 때문에, lock이 필요하지 않은 상황이더라도 무조건 lock을 걸어서 성능상 문제가 될 수 있다. 특히 read 작업이 많이 일어나는 경우 단점이 될 수 있다. 또한, 선착순 이벤트같이 많은 트래픽이 몰리는 상황이나 여러 테이블에 lock을 걸면서 서로 자원이 필요한 경우, 데드락이 발생할 수 있고 이는 비관적 락으로 해결할 수 없는 부분이다. |
- **분산락 (distributed lock)**
    - 여러 컴퓨터 또는 프로세스 간에 공유된 자원에 대한 동시 액세스를 제어하기 위해 사용된다.
    - **분산 시스템에서 동시성 문제를 해결**하기 위해 사용되며, **분산된 서버 또는 클러스터 간**의 상호작용이 필요하다.
    - 주로 **데이터베이스나 메시지 큐 등의 분산 시스템**에서 사용된다.
    - 대표적인 분산락 기법으로는 ZooKeeper, Redis 등이 있다.
    - **Redis**는 RedLock이라는 알고리즘을 제안하며 3가지 특성을 보장해야한다고 한다.
        - 오직 한 순간에 하나의 작업자만이 락(lock) 을 걸 수 있다.
        - 락 이후, 어떠한 문제로 인해 락을 풀지 못하고, 종료된 경우라도 다른 작업자가 락을 획득할 수 있어야합니다.
        - Redis 노드가 작동하는한, 모든 작업자는 락을 걸고 해체할 수 있어야합니다.
    - 분산 락을 구현하기 위해 lock에 대한 정보를 Redis에 저장하고 있어야한다. 그리고 분산환경에서 여러대의 서버들은 공통된 Redis를 바라보며, 자신이 공유 자원에 접근할 수 있는지 확인한다.
- **스핀락 (spin lock)**
    - **자원에 대한 접근이 필요할 때 무한루프를 돌면서 반복적으로 확인**하며, 다른 스레드가 lock을 해제할 때까지 대기한다.
    - 경쟁 상태 (2개 이상의 프로세스가 공유 자원을 동시에 읽거나 쓰는 상황)가 짧고 자원 점유 시간이 길지 않은 경우에 효과적이다.
    - 주로 멀티코어 시스템에서 사용되며, 락 획득을 위해 CPU를 계속 사용하므로 서버에 많은 부하를 주어 주의해야 한다.
