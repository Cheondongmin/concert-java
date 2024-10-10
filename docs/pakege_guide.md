## 패키지 구조
* clean + layered 아키텍처

이 구조는 Clean Architecture 원칙을 적용해 비즈니스 로직을 핵심으로 하고, 외부 시스템이나 프레임워크에 대한 의존성을 최소화합니다.<br>
Layered Architecture를 활용해 각 계층의 역할을 명확히 구분함으로써 유연한 확장성, 테스트 용이성, 높은 유지보수성을 제공합니다.

```shell
app	
	└─ domain
		└─ concert
			└─ entity
			└─ service
			└─ repository
		└─ user
			└─ entity
			└─ service
			└─ repository
		└─ queue
			└─ entity
			└─ service
			└─ repository
	└─ interfaces
		└─ common
		└─ v1
		  └─ concert
			└─ req
			└─ res
			└─ ConcertController.java
			└─ ConcertControllerException.java			
		  └─ queue
			└─ req
			└─ res
			└─ QueueController.java
			└─ QueueCntrollerException.java
		  └─ user
			└─ req
			└─ res
			└─ UserController.java
		    └─ UserCntrollerException.java
	└─ infrastructure
		└─ kafka
		└─ redis
config
	└─ SwaggerConfig.java
```
