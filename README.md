회고: validator 추가 및 더 세분화된 예외 클래스를 만들어서 예외 처리 개선 필요
, access token 등 토큰 갱신 매커니즘 활용, 암호화 알고리즘 코드 개선 등으로 보안을 강화 필요
, 동시 접속자 증가할 경우 redis 캐시를 mongoDB 활용으로 저장 속도, 데이터 신뢰도 강화 필요
, 주고 받는 구조가 더 복잡해질 경우 상황에 맞게 Kafka 또는 rabbitMQ를 활용하여 대용량 데이터 처리 개선 필요
, 시간상 성공 및 실패 테스트 코드를 많이 작성 못해서 아쉬웠음

- 구현 내용

- 사용자 API

- 회원가입
엔드포인트: POST /api/user/signup
설명: 새로운 사용자 등록
요청 본문: SignUpRequest (userId, password, idType, idValue, name)
응답:
성공: 200 OK와 SignUpResponse
오류: 400 Bad Request와 오류 메시지

주요 기능 : 
새로운 사용자를 등록합니다. 
이메일 형식의 사용자 ID, 비밀번호, ID 타입(주민등록번호 또는 사업자등록번호), ID 값, 이름을 입력받습니다.
입력된 정보의 유효성을 검사합니다.
비밀번호는 암호화하여 저장합니다.
ID 값은 암호화하여 저장합니다.

- 로그인
엔드포인트: POST /api/user/login
설명: 사용자 인증 및 JWT 토큰 생성
요청 본문: LoginRequest (userId, password)
응답:
성공: 200 OK와 LoginResponse (JWT 토큰 포함)
오류: 401 Unauthorized와 ErrorResponse

주요 기능 : 사용자 ID와 비밀번호로 인증을 수행합니다.
인증 성공 시 User 객체를 반환합니다.

- 송금 API

견적 받기
엔드포인트: POST /transfer/quote
설명: 송금을 위한 견적 생성
헤더: Authorization (JWT 토큰)
요청 본문: QuoteRequest
응답:
성공: 200 OK와 QuoteResponse
오류: 400 Bad Request와 오류 메시지

주요 기능 :
송금 요청에 대한 견적을 생성합니다.
JWT 토큰을 통해 사용자 인증을 수행합니다.

- 송금 요청
엔드포인트: POST /transfer/request
설명: 송금 요청 처리
헤더: Authorization (JWT 토큰)
요청 본문: TransferRequest
응답:
성공: 200 OK와 TransferResponse
오류:
400 Bad Request (QUOTE_EXPIRED 또는 LIMIT_EXCESS)
500 Internal Server Error (알 수 없는 오류)

주요 기능 :
실제 송금 요청을 처리합니다.
JWT 토큰을 통해 사용자 인증을 수행합니다.
일일 한도 초과 및 견적 만료 등의 예외 상황을 처리합니다.

- 송금 내역 조회
엔드포인트: GET /transfer/list
설명: 사용자의 송금 내역 조회
헤더: Authorization (JWT 토큰)
응답: 200 OK와 TransferHistoryResponse

주요 기능 :
사용자의 송금 내역을 조회합니다.
JWT 토큰을 통해 사용자 인증을 수행합니다.

인증
모든 송금 API 엔드포인트는 Authorization 헤더에 유효한 JWT 토큰이 필요합니다. 
UserController는 로그인 성공 시 이 토큰을 생성합니다.

의존성
Spring Boot
Spring Security
JWT (인증용)
Lombok

