package com.sai.slog.app.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.slog.app.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 08/01/17.
 */
public class LogService {

    private static final RestTemplate rest = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<String> allCustomers() {
        List<String> customers = new ArrayList<>();

        Map<String, Object> rq = new HashMap<>();
        rq.put("showAllCustomers", true);

        List response = rest.postForObject(System.getProperty("log.search.endpoint").trim(), rq, List.class);
        response.forEach(res -> customers.add(((Map) res).get("key").toString()));
        return customers;
    }

    public List<String> allComponents() {
        List<String> components = new ArrayList<>();

        Map<String, Object> rq = new HashMap<>();
        rq.put("showAllComponents", true);

        List response = rest.postForObject(System.getProperty("log.search.endpoint").trim(), rq, List.class);
        response.forEach(res -> components.add(((Map) res).get("key").toString()));
        return components;
    }

    public List<Log> logsSearch(String customer, String component, String logLevel, Date fromDate, Date toDate, String text, int offset, String fileName, String ipAddress) {
        List<Log> logs = new ArrayList<>();
        Map<String, String> criteria = new HashMap<>();
        criteria.put("customer", customer);
        criteria.put("component", component);
        criteria.put("fileName", fileName);
        criteria.put("ipAddress", ipAddress);
        criteria.put("fromPage", offset + "");
        criteria.put("toPage", "1000");
        criteria.put("logLevel", logLevel);
        if (fromDate != null) {
            criteria.put("fromDate", DATE_FORMAT.format(fromDate));
        }
        if (toDate != null) {
            criteria.put("toDate", DATE_FORMAT.format(toDate));
        }
        criteria.put("freeTextSearch", text);
        List response = rest.postForObject(System.getProperty("log.search.endpoint").trim(), criteria, List.class);
        response.forEach(res -> logs.add(objectMapper.convertValue(res, Log.class)));
        return logs;
    }

    public Set<StepLookupIndex> flowIndexes(String customer, Date fromDate, Date toDate, String flowFunctionality) {
        Set<StepLookupIndex> logs = new LinkedHashSet<>();

        if (fromDate == null) {
            fromDate = new Date(1111);
        }
        if (toDate == null) {
            toDate = new Date(System.currentTimeMillis());
        }
        String endpoint = String.format(System.getProperty("step.index.search.endpoint"), customer, fromDate.getTime(), toDate.getTime());
        if (StringUtils.isNotBlank(flowFunctionality)) {
            endpoint = endpoint + "&flowFunctionality=" + flowFunctionality.trim();
        }
        System.out.println(endpoint);
        List response = rest.getForObject(endpoint, List.class);
        List<String> stepIdsWithErrors = new ArrayList<>();
        response.forEach(s -> {
            String logLevel = ((Map) s).get("logLevel").toString();
            if (logLevel.equals("ERROR")) {
                stepIdsWithErrors.add(((Map) s).get("sessionId").toString());
            }
        });

        response.forEach(res -> {
            StepLookupIndex stepLookupIndex = objectMapper.convertValue(res, StepLookupIndex.class);
            logs.add(stepLookupIndex);
            if (stepIdsWithErrors.contains(stepLookupIndex.getSessionId())) {
                stepLookupIndex.setLogLevel("ERROR");
            } else {
                stepLookupIndex.setLogLevel("INFO");
            }
        });
        return logs;
    }

    public Set<Step> viewSteps(String customer, String sessionId, String stepCategory) {
        Set<Step> logs = new LinkedHashSet<>();
        List response = rest.getForObject(String.format(System.getProperty("step.search.endpoint"), customer, sessionId, stepCategory), List.class);
        response.forEach(res -> logs.add(objectMapper.convertValue(res, Step.class)));
        return logs;
    }

    public List<String> logFileNames(final String component) {
        List<String> components = new ArrayList<>();

        Map<String, Object> rq = new HashMap<>();
        rq.put("showAllFilesPerComponent", true);
        rq.put("component", component);

        List response = rest.postForObject(System.getProperty("log.search.endpoint").trim(), rq, List.class);
        response.forEach(res -> components.add(((Map) res).get("key").toString()));
        return components;
    }

    public void saveAlertConfig(final AlertConfig selectedAlertConfig) {
        rest.postForObject(System.getProperty("alert.config.save.endpoint").trim(), selectedAlertConfig, Map.class, new HashMap<>());
    }

    public List<AlertConfig> alertConfigs(String selectedCustomer) {
        List configs = rest.getForObject(String.format(System.getProperty("alert.config.list.endpoint").trim(), selectedCustomer), List.class);
        return (List<AlertConfig>) configs.stream().map(c -> objectMapper.convertValue(c, AlertConfig.class)).collect(toList());
    }

    public List<ExceptionCorrelation> recommendations(String customer, long start, long end, String messageFreeText) {
        String recoUrl = "http://localhost:9980/exceptioncorrelations?customer=%s&exceptionMessage=%s&startTimeInMillis=%s&endTimeInMillis=%s";
        System.out.println(String.format(recoUrl, customer, messageFreeText, start, end));
        List response = rest.getForObject(String.format(recoUrl, customer, messageFreeText, start, end), List.class);
        return (List<ExceptionCorrelation>) response.stream().map(c -> objectMapper.convertValue(c, ExceptionCorrelation.class)).collect(toList());
    }

    public static void main(String[] args) {
        String recoUrl = "http://localhost:9980/exceptioncorrelations?customer=%s&exceptionMessage=%s&startTimeInMillis=%s&endTimeInMillis=%s";
        List response = rest.getForObject(String.format(recoUrl, "internal", "Provisioning", 1, System.currentTimeMillis()), List.class);
        System.out.println(response);
    }
}
