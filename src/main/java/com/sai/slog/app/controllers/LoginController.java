package com.sai.slog.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.slog.app.model.LoginBean;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 07/01/17.
 */
@Data
public class LoginController {

    private String userName = "guest";
    private String password = "XPQjsXeT";

    public void login() throws Exception {
        if (!verify(userName, password)) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Invalid Login!",
                            "Please Try Again!"));
        } else {
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            req.getSession(true).setAttribute("user", new LoginBean(userName, userName));
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.do");
        }
    }

    private boolean verify(String userName, String password) throws Exception {
        List users = new ObjectMapper().readValue(LoginController.class.getClassLoader().getResourceAsStream("users.json"), List.class);
        return users.stream().filter(userObj -> {
            Map user = (Map) userObj;
            return user.get("userId").toString().equalsIgnoreCase(userName) && user.get("password").toString().equals(DigestUtils.md5Hex(password));
        }).findAny().isPresent();
    }
}
