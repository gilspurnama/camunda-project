package com.example.workflow.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class UserDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response{
        private String id;
        private String firstName;
        private String lastName;
        private String email;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserCredential{
        private String userId;
        private String email;
        private List<String> roleName;
    }

}
