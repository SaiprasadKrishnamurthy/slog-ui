package com.sai.slog.app.controllers;

import com.sai.slog.app.model.AlertConfig;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.springframework.web.client.RestTemplate;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class AlertConfigController {

    private List<AlertConfig> configs = new ArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private LogService logService = new LogService();
    private List<String> customers = new ArrayList<>();
    private String selectedCustomer;
    private AlertConfig selectedAlertConfig = new AlertConfig();
    private List<AlertConfig> filteredConfigs = new ArrayList<>();


    public AlertConfigController() {
        String name = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("name");
        if (StringUtils.isNotBlank(name)) {
            customers = logService.allCustomers();
            for (String customer : customers) {
                List<AlertConfig> configs = logService.alertConfigs(customer);
                Optional<AlertConfig> c = configs.stream().filter(cn -> cn.getName().equals(name)).findFirst();
                if (c.isPresent()) {
                    selectedAlertConfig = c.get();
                    break;
                }
            }
        } else {
            customers = logService.allCustomers();
        }
    }

    public void save() {
        logService.saveAlertConfig(selectedAlertConfig);
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Template Saved");
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void onSelectCustomer() {
        filteredConfigs = logService.alertConfigs(selectedCustomer);
    }
}
