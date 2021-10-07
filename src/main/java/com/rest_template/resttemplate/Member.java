package com.rest_template.resttemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Member {
    private Integer id;
    private String name;

    Member(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
