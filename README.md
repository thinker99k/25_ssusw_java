# JAVACHAT

## Login
(clnt->serv) ```(string) (string)\n```\
-> 로그인 요청

(serv->clnt) ```(int)\n```\
-> 로그인 쿼리 결과
>100 : LOGIN_OK
\
>200 : WRONG_NO
\
>300 : TOO_MANY


## Message Structure
```[ 0~1번 나옴 ]```
\
```{ 0번~n번 나옴 }```
\
```( 1번 나옴 )```

**readline은 \n, \r 이전의 모든 것을 받음**

### 0 : 일반 메세지

```0 [string]\n```

### 1 : heartbeat

(clnt->serv) ```1 true\n```\
-> 어떤 클라이언트의 heartbeat

(serv->clnt) ```1 (name) (boolean) }\n```\
-> 다른 클라이언트(name)의 online status
