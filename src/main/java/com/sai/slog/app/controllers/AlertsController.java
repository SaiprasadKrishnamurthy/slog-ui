package com.sai.slog.app.controllers;

import com.sai.slog.app.model.AlertConfig;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.primefaces.model.chart.MeterGaugeChartModel;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AlertsController {

    private List<MeterGaugeChartModel> model = new ArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private LogService logService = new LogService();
    private final List<AlertConfig> configs = new ArrayList<>();


    public AlertsController() {
        List<String> customers = logService.allCustomers();
        for (String customer : customers) {
            configs.addAll(logService.alertConfigs(customer));
        }

        for (AlertConfig c : configs) {
            MeterGaugeChartModel m = new MeterGaugeChartModel();
            m.setTitle(c.getName());
            m.setGaugeLabel("count");
            m.setSeriesColors("66cc66,93b75f,E7E658,cc6666,cc0000");
            m.setMouseoverHighlight(true);
            m.setShadow(true);
            List<Number> intervals = new ArrayList<Number>() {{
                add(c.getLeastCountThreshold());
                add(c.getLowCountThreshold());
                add(c.getMediumCountThreshold());
                add(c.getHighCountThreshold());
                add(c.getHighestCountThreshold());
            }};
            m.setIntervals(intervals);
            Map count = restTemplate.getForObject(String.format(System.getProperty("alerts.endpoint"), c.getTimeWindowInDays(), c.getKeyword()), Map.class);
            int count1 = Integer.parseInt(count.get("count").toString());
            if (count1 > c.getHighestCountThreshold()) {
                count1 = c.getHighestCountThreshold();
            }
            m.setValue(count1);
            model.add(m);
        }
    }

}