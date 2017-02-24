package com.sai.slog.app.controllers.intelligence;

import com.sai.slog.app.model.*;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.primefaces.extensions.component.gchart.model.GChartModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.diagram.DefaultDiagramModel;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by saipkri on 03/02/17.
 */
@Data
public class SimpleRecomendController {

    private LogService logService = new LogService();
    private List<String> components = new ArrayList<>();
    private List<String> customers = new ArrayList<>();
    private List<Log> logs = new ArrayList<>();
    private Set<StepLookupIndex> stepIndex;
    private Set<Step> steps;
    private String component;
    private String customer;
    private String logLevel;
    private String messageFreeText;
    private Date fromDate = null;
    private Date toDate = null;
    private String file;
    private DefaultDiagramModel model;
    private TreeNode root;
    private boolean renderDiagram;
    private GChartModel flow = null;
    private String seq;
    private boolean showLogs;
    private boolean searchResultsFound;
    private List<LogFile> files = new ArrayList<>();
    private Map<String, Integer> offsets = new HashMap<>();
    private int logFileTabIndex;
    private String flowFunctionality;
    private String ipAddress;
    private int aroundMinutes = 1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    private List<ExceptionCorrelation> recommendations;

    public SimpleRecomendController() {
        customers = logService.allCustomers();
        components = logService.allComponents();
    }

    public void recommend() {
        System.out.println("Curr Timestamp: "+System.currentTimeMillis());
        System.out.println("From Timestamp: "+(System.currentTimeMillis() - (1000 * 60 * aroundMinutes)));
        recommendations = logService.recommendations(customer, (System.currentTimeMillis() - (1000 * 60 * aroundMinutes)), System.currentTimeMillis(), messageFreeText);
        System.out.println(recommendations.size());
        searchResultsFound = true;
    }

}