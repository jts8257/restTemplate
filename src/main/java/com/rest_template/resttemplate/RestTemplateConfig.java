package com.rest_template.resttemplate;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


//RestTemplate은 자동으로 빈 등록되지 않기 때문에 직접 등록해줘야 한다.

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

        /* Message Converter */
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(List.of(MediaType.TEXT_HTML));

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(mappingJackson2HttpMessageConverter);
        messageConverters.add(new StringHttpMessageConverter());

        /* add 와 set 둘중 하나만 하면 된다.*/
        //restTemplate.setMessageConverters(messageConverters); // set
        /*
        converter.getClass() : class org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.StringHttpMessageConverter
         */

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // add
        /*
        converter.getClass() : class org.springframework.http.converter.StringHttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.ByteArrayHttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.StringHttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.ResourceHttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.xml.SourceHttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
        converter.getClass() : class org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
         */
        for (HttpMessageConverter converter : restTemplate.getMessageConverters()) {
            log.info("converter.getClass() : {}", converter.getClass());
        }
        return restTemplate;
    }
}

