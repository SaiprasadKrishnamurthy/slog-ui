package com.sai.slog.app.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.slog.app.model.Member;
import com.sai.slog.app.service.LogService;
import lombok.Data;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 13/01/17.
 */
@Data
public class OurCoreTeamController {

    private LogService service = new LogService();
    private List<Member> members;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public OurCoreTeamController() throws Exception {
        List users = MAPPER.readValue(LoginController.class.getClassLoader().getResourceAsStream("users.json"), List.class);
        members = (List<Member>) users.stream().map(userObj -> MAPPER.convertValue(userObj, Member.class)).collect(toList());
    }
}
