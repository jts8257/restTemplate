package com.rest_template.resttemplate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class RestTemplateTest {

    private final RestTemplate template = new RestTemplate();

    @Test
    void simp_get1() throws Exception {
        String reqUri = "http://localhost:7070/api/simp/member/{id}";
        Map<String, Integer> params = new HashMap<>();
        params.put("id", 1);

        Member member = template.getForObject(reqUri, Member.class, params);
        Assertions.assertThat(member.getName()).isEqualTo("name1");
    }

    @Test
    void simp_get2() throws Exception {
        String reqUri = "http://localhost:7070/api/simp/member";
        Map<String, Integer> params = new HashMap<>();
        params.put("id", 1);

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(reqUri)
                .path("/{id}")
                .encode()
                .buildAndExpand(params);

        Member member = template.getForObject(builder.toUri(), Member.class);
        Assertions.assertThat(member.getName()).isEqualTo("name1");
    }

    @Test
    void simp_post1() throws Exception {
        String reqUri = "http://localhost:7070/api/simp/member";

        Member member = new Member(6, "name6");
        Member member2 = template.postForObject(reqUri, member,Member.class);
        Assertions.assertThat(member2.getName()).isEqualTo("name6");
    }

    @Test
    void simp_post2() throws Exception {
        String reqUri = "http://localhost:7070/api/simp/member";

        Member member = new Member(7, "name7");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","test key");

        Member member2 = template.postForObject(reqUri, new HttpEntity<>(member, headers),Member.class);
        Assertions.assertThat(member2.getName()).isEqualTo("name7");
    }
}
