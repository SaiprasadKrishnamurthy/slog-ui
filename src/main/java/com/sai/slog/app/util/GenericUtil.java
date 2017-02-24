package com.sai.slog.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by saipkri on 13/01/17.
 */
public final class GenericUtil {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static final class User {
        private String userId;
        private String password;
        private String passwordText;
    }

    private GenericUtil() {
    }

    public static void accounts() throws Exception {
        List<String> userIds = Arrays.asList("suresh.s@studentpodium.com", "saiprasad.k@studentpodium.com", "karthik.s@studentpodium.com", "prabhu.s@studentpodium.com", "kumar.t@studentpodium.com", "pankaj.j@studentpodium.com", "lakshman.k@studentpodium.com");

        List<User> users = new ArrayList<>();

        for (String user : userIds) {
            String data = RandomStringUtils.randomAlphanumeric(8);
            users.add(new User(user, new String(DigestUtils.md5Hex(data)), data));
        }

        System.out.println(new ObjectMapper().writeValueAsString(users));
    }

    public static void main(String[] args) throws Exception {
        accounts();
    }
}
