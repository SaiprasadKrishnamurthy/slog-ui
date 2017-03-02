package com.sai.slog.app.controllers.intelligence;

import com.sai.slog.app.model.*;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.component.gchart.model.GChartModel;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

import javax.faces.application.FacesMessage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

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
    private String description;
    private String component;
    private String logMessage;
    private int aroundMinutes = 1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    private List<ExceptionCorrelation> recommendations;
    private List<ExceptionCorrelation> detrimentalErrors;
    private Set<LogCorrelation> logCorrelations;
    private List<MindmapNode> lookups = new ArrayList<>();
    private MindmapNode root;
    private int backMinutes;
    private String nodesJson = "{}";
    private String edgeJson = "{}";
    private String srcLc;
    private List<String> causeLc;


    public SimpleRecomendController() {
        customers = logService.allCustomers();
        components = logService.allComponents();
        logCorrelations = logService.correlations("reports");
        System.out.println(logCorrelations);
        root = new DefaultMindmapNode("reports", "reports");
        buildGraph(logCorrelations);
    }

    private void buildGraph(Set<LogCorrelation> logCorrelations) {
        nodesJson = logCorrelations.stream().map(lc -> String.format("{id: '%s', label: '%s', shape: 'box', font: {'face': 'Monospace'}, color: {background:'pink', border:'purple'}}", lc.getDescription(), lc.getDescription())).collect(joining(","));
        edgeJson = new ArrayList<>(logCorrelations).stream()
                .flatMap(lc -> lc.getDirectlyCausedBy().stream().map(chils -> {
                    String edgeTmplate = "    {from: '%s', to: '%s', length: 250, label: 'cause', arrows: 'to', font: {align: 'middle'}}";
                    return String.format(edgeTmplate, lc.getDescription(), chils.getDescription());
                })).collect(joining(","));
    }

    public void recommend() {
        System.out.println("Curr Timestamp: " + System.currentTimeMillis());
        System.out.println("From Timestamp: " + (System.currentTimeMillis() - (1000 * 60 * aroundMinutes)));
        recommendations = logService.recommendations(customer, (System.currentTimeMillis() - (1000 * 60 * aroundMinutes)), System.currentTimeMillis(), messageFreeText);
        System.out.println(recommendations.size());
        searchResultsFound = true;
    }

    public String command(String command, String[] params) {
        if (command.equalsIgnoreCase("LINK")) {
            String args = Stream.of(params).collect(joining(" "));
            StringTokenizer tkn = new StringTokenizer(args, "->");
            LogCorrelation src = null;
            LogCorrelation cause = null;

            while (tkn.hasMoreTokens()) {
                String token = tkn.nextToken().trim();
                System.out.println(token);
                if (token.trim().length() > 0) {
                    final String _token = token.replace("(", "").replace(")", "");
                    Optional<LogCorrelation> logCorrelation = logCorrelations.stream().filter(lc -> lc.getDescription().equals(_token)).findFirst();
                    if (!logCorrelation.isPresent()) {
                        return "Cannot find a rule with description '" + _token + "'";
                    } else {
                        if (src == null) {
                            src = logCorrelation.get();
                        } else {
                            cause = logCorrelation.get();
                        }
                    }
                }
            }
            src.getDirectlyCausedBy().add(cause);
            buildGraph(logCorrelations);
            RequestContext context = RequestContext.getCurrentInstance();
            //(do things here)
            context.execute("rc();");
            return "Object created";
        }
        return "Bad command";
    }

    public void customerSelect() {
        System.out.println("Customer: " + customer);
    }

    public void link() {
        System.out.println("Src: " + srcLc);
        System.out.println("Causes: " + causeLc);

        LogCorrelation src = logCorrelations.stream().filter(l -> l.getDescription().equals(srcLc)).findFirst().get();
        src.getDirectlyCausedBy().clear();
        for (String c : causeLc) {
            src.addDirectCause(logCorrelations.stream().filter(l -> l.getDescription().equals(c)).findFirst().get());
        }
        buildGraph(logCorrelations);
        logService.save(logCorrelations);
    }

    public void createRule() {
        LogCorrelation lc = new LogCorrelation();
        lc.setDescription(description.trim());
        lc.setComponent(component.trim());
        lc.setLogMessage(logMessage.trim());
        logCorrelations.add(lc);
        System.out.println("Created: " + logCorrelations);
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Rule Saved");
        RequestContext.getCurrentInstance().showMessageInDialog(message);
        buildGraph(logCorrelations);
    }

    public void detrimental() {
        System.out.println(backMinutes);
        detrimentalErrors = logService.detrimental(customer, backMinutes);
        searchResultsFound = true;
    }
}
