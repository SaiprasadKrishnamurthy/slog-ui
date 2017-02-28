package com.sai.slog.app.controllers.intelligence;

import com.sai.slog.app.model.*;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.primefaces.extensions.component.gchart.model.GChartModel;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

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
    private List<ExceptionCorrelation> detrimentalErrors;
    private List<LogCorrelation> logCorrelations;
    private List<MindmapNode> lookups = new ArrayList<>();
    private MindmapNode root;
    private int backMinutes;

    public SimpleRecomendController() {
        customers = logService.allCustomers();
        components = logService.allComponents();
        logCorrelations = logService.correlations("reports");
        root = new DefaultMindmapNode("reports", "reports");
        buildTree(logCorrelations, root);
    }

    private void buildTree(List<LogCorrelation> logCorrelations, MindmapNode root) {
        for (LogCorrelation logCorrelation : logCorrelations) {
            System.out.println(logCorrelation);
            Optional<MindmapNode> existing = lookups.stream().filter(m -> m.getData().equals(logCorrelation.getDescription())).findFirst();

            MindmapNode child = existing.isPresent() ? existing.get() : new DefaultMindmapNode(logCorrelation.getDescription(), logCorrelation.getDescription());
            if (!existing.isPresent()) {
                lookups.add(child);
            }
            root.addNode(child);
            if (logCorrelation.getDirectlyCausedBy() != null && !logCorrelation.getDirectlyCausedBy().isEmpty()) {
                buildTree(new ArrayList<>(logCorrelation.getDirectlyCausedBy()), child);
            }
        }
    }

    public void recommend() {
        System.out.println("Curr Timestamp: " + System.currentTimeMillis());
        System.out.println("From Timestamp: " + (System.currentTimeMillis() - (1000 * 60 * aroundMinutes)));
        recommendations = logService.recommendations("internal", (System.currentTimeMillis() - (1000 * 60 * aroundMinutes)), System.currentTimeMillis(), messageFreeText);
        System.out.println(recommendations.size());
        searchResultsFound = true;
    }

    public void customerSelect() {
        System.out.println("Customer: "+customer);
    }
    public void detrimental() {
        System.out.println(backMinutes);
        detrimentalErrors = logService.detrimental(customer, backMinutes);
        searchResultsFound = true;
    }
}
