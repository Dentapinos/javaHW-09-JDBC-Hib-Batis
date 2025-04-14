package org.example.enums;

import lombok.Getter;

@Getter
public enum SessionName {
    HIBERNATE("hibernate"),
    JDBC("jdbc"),
    MY_BATIS("mybatis");

    private final String sessionName;

    SessionName(String sessionName) {
        this.sessionName = sessionName;
    }
}
