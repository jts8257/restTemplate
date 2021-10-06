# RestTemplate
RestTemplate 는 서버에서 다른 서버로 HTTP 메시지를 전송하고 응답을 받을 때 사용하는 클래스이다. 

- RestTemplate 는 spring 에서 제공하는 Http Client 로 REST API를 호출을 위한 함수를 제공하는 클래스이다. 
- Http 요청 후 Json, xml, String 과 같은 형식으로 응답을 받을 수 있다. 
- Blocking I/O 기반의 동기적인 API 방식을 이용한다.


## RestTemplate 외에 다른 Http Client
Spring 에서 이용할 수 있는 다른 Http Client 로는 HttpURLConnection, HttpClient(HttpComponent), WebClient(비동기 방식) 등이 있다.

## RestEmplate 와 Connection Pooling
RestTemplate은 기본적으로 Connection Pooling을 지원하지 않기 때문에 호출 할 때마다 로컬 포트를 열고 tcp connection을 만들어 사용한다. <br>
사용한 소켓은 TIME_WAIT 상태가 되어 요청이 많은 시점에도 재 사용하지 못하고 장애의 요소가 된다.<br>
이를 방지하기 위해 connection pool을 사용해 커넥션을 재 사용하고 제한 할 필요가 있다. <br>
RestTemplate이 기본적으로 사용하는 URLConnection 대신 apache에서 제공하는 HttpClient를 사용하면 connection pooling이 가능해진다

```java
// default
@Slf4j
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        log.info("restTemplate.getRequestFactory().getClass() : {}", restTemplate.getRequestFactory().getClass());
        // restTemplate.getRequestFactory().getClass() : class org.springframework.http.client.SimpleClientHttpRequestFactory
        // restTemplate 는 디폴트로 SimpleClientHttpRequestFactory 를 이용해 http client 를 만들게됨.
        return restTemplate;
    }
}
```
apache HttpClient 를 이용하기 위해 의존성 추가
```bash
implementation group: 'org.apache.httpcomponents', name: 'httpclient', version: '4.5.13'
```

```java
@Slf4j
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        factory.setReadTimeout(5000); // read timeout
        factory.setConnectTimeout(3000); // connection timeout

        // import org.apache.http.client.HttpClient;
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(50) // 최대 커넥션 수
                .setMaxConnPerRoute(20).build(); // 각 호스트 (IP와 port 조합) 당 커넥션 풀에 생성 가능한 커넥션 수

        factory.setHttpClient(httpClient); // HttpComponentsClientHttpRequestFactory 에 설정값 주입.

        RestTemplate restTemplate = new RestTemplate(factory); // HttpComponentsClientHttpRequestFactory 를 이용해서 RestTemplate 생성

        log.info("restTemplate.getRequestFactory().getClass() : {}", restTemplate.getRequestFactory().getClass());
        // restTemplate.getRequestFactory().getClass() : class org.springframework.http.client.HttpComponentsClientHttpRequestFactory
        return restTemplate;
    }
}
```

## Message Converter
Message Converter 는 Java 객체를 Request, Response 의 body 로 변환할때 사용된다. <br> 
Http Message 에서는 body의 타입을 Content-type 라는 header 값에 명시한다. <br>
이렇게 명시된 Content-type에 따라 적절한 Message Converter 를 찾게된다. <br>
만약 명시하지 않는다면, Message Converter 에 내재된 방식으로 적절한 type 을 찾아서 변환하게 된다. <br>
스프링은 클라이언트 측 RestTemplate과 서버 측 RequestMehtodHandlerAdater에 메시지 컨버터를 자동으로 등록준다. <br>

이러한 Message Converter 는 일상적으로 사용되고 있는데, RestController 에서 각 메서드의 argument 와 return type 을<br>
Object 로 했음에도 Json 타입이 Object 로, Object 가 Json 으로 변환되어졌던 것은 모두 Message Converter 가 동작한 것이다.

우리가 RestTemplate 를 설정할때 (@Bean) 이러한 Message Converter 를 추가(add) 혹은 세팅(set)해서 사용할 수 있다.<br>
add 를 할 경우 Spring이 제공하는 Message Converter 에 더해서 사용하는 것이고, <br>
set 을 할 경우 Spring이 제공하는 Message Converter 를 이용하지 않고, 특정된 Converter 만 이용하게 된다.

어떤 설정도 하지 않은 상태에서 RestTemplate 가 갖고있는 Message Converter 를 확인해보면, Spring 에서 기본으로 제공하는 객체를 확인할 수 있다. 
```java
@Slf4j
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        factory.setReadTimeout(5000);
        factory.setConnectTimeout(3000);
        
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(20).build();

        factory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        log.info("restTemplate.getRequestFactory().getClass() : {}", restTemplate.getRequestFactory().getClass());

        /* 코드 추가 예정 */
        for (HttpMessageConverter converter : restTemplate.getMessageConverters()) {
            log.info("converter.getClass() : {}", converter.getClass());
        }
        /* 출력 결과
          converter.getClass() : class org.springframework.http.converter.ByteArrayHttpMessageConverter
          converter.getClass() : class org.springframework.http.converter.StringHttpMessageConverter
          converter.getClass() : class org.springframework.http.converter.ResourceHttpMessageConverter
          converter.getClass() : class org.springframework.http.converter.xml.SourceHttpMessageConverter
          converter.getClass() : class org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
          converter.getClass() : class org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
          converter.getClass() : class org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
         */
        return restTemplate;
    }
}
```

위에서 코드 추가 예정 부분에 아래의 코드를 추가하면 set 을 했기 때문에 다른 Message Converter 들은 보이지 않고, 대신 설정된 Message Converter 만 나오는게 보인다. 

```java
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(List.of(MediaType.TEXT_HTML));

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(mappingJackson2HttpMessageConverter);
        messageConverters.add(new StringHttpMessageConverter());

        restTemplate.setMessageConverters(messageConverters);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        /* 출력 결과
                converter.getClass() : class org.springframework.http.converter.StringHttpMessageConverter
                converter.getClass() : class org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
                converter.getClass() : class org.springframework.http.converter.StringHttpMessageConverter
         */
```

# Q 왜 로그가 2번 찍힐까? Spring 이 아니라 Spring boot 여서 그런가?