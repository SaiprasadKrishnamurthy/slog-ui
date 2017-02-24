package com.sai.slog.app.controllers.information;

import com.sai.slog.app.model.Log;
import com.sai.slog.app.model.LogFile;
import com.sai.slog.app.model.Step;
import com.sai.slog.app.model.StepLookupIndex;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.primefaces.extensions.component.gchart.model.GChartModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.Element;

import javax.faces.context.FacesContext;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by saipkri on 03/02/17.
 */
@Data
public class LogSearchController {

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


    public LogSearchController() {
        customers = logService.allCustomers();
        components = logService.allComponents();
    }

    public void searchIssuesAround() throws Exception {
        System.out.println(" Around Miutes: " + aroundMinutes);
        System.out.println(" Around Miutes: " + aroundMinutes);
        String timestamp = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("timestamp");
        Date from = sdf.parse(timestamp);
        long to = from.getTime() + (aroundMinutes * 60 * 1000);
        Date toDate = new Date(to);
        Date fromDate = new Date(from.getTime() - 3000);
        aroundSearch(fromDate, toDate);
    }

    private void aroundSearch(Date fromDate, Date toDate) {
        List<String> logFileNames = logService.logFileNames(component);

        // Don't include the ones that are already in the view.
        List<String> otherLogs = logFileNames.stream().filter(lf -> !this.files.stream().anyMatch(l -> l.getFileName().equals(lf))).collect(Collectors.toList());

        for (String logFile : otherLogs) {
            logs = logService.logsSearch(customer, "", "", fromDate, toDate, "", 0, logFile, ipAddress);
            offsets.put(logFile, 1000);
            LogFile lf = new LogFile();
            lf.setFileName(logFile);
            lf.setLogs(logs);
            this.files.add(lf);
        }
        searchResultsFound = true;
    }

    public void search() {
        this.files.clear();
        List<String> logFileNames = logService.logFileNames(component);

        for (String logFile : logFileNames) {
            logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, 0, logFile, ipAddress);
            offsets.put(logFile, 1000);
            LogFile lf = new LogFile();
            lf.setFileName(logFile);
            lf.setLogs(logs);
            this.files.add(lf);
        }
        searchResultsFound = true;
    }

    public void flowSearch() {
        stepIndex = logService.flowIndexes(customer, fromDate, toDate, flowFunctionality);
        showLogs = true;
    }

    public void viewSteps() {
        String sessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("sessionId");
        String stepCategory = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("functionality");
        steps = logService.viewSteps(customer, sessionId, stepCategory);
        System.out.println(" -------- ");
        System.out.println(steps);
        System.out.println("----------\n\n");
        draw(steps);
    }

    public void prevSet() {
        String _fileName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("fileName");
        LogFile logFile = files.stream().filter(lf -> lf.getFileName().equals(_fileName)).findFirst().get();
        int start = logFile.getStartRecord();
        start = start - 1000;
        logFile.setStartRecord((start <= 0) ? 0 : start);
        logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, logFile.getStartRecord(), logFile.getFileName(), ipAddress);
        logFile.setLogs(logs);
        logFileTabIndex = IntStream.range(0, files.size()).filter(i -> files.get(i).getFileName().equals(_fileName)).findFirst().getAsInt();
    }

    public void nextSet() {
        String _fileName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("fileName");
        LogFile logFile = files.stream().filter(lf -> lf.getFileName().equals(_fileName)).findFirst().get();
        int start = logFile.getStartRecord();
        start = start + 1000;
        logFile.setStartRecord((start < 0) ? 0 : start);
        logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, logFile.getStartRecord(), logFile.getFileName(), ipAddress);
        logFile.setLogs(logs);
        logFileTabIndex = IntStream.range(0, files.size()).filter(i -> files.get(i).getFileName().equals(_fileName)).findFirst().getAsInt();
    }

    private void draw(final Set<Step> steps) {
        this.files.clear();
        List<Step> st = new ArrayList<>(steps);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (i != steps.size() - 1) {
                long timeElapsed = (st.get(i + 1).getTimestamp() - st.get(i).getTimestamp()) / 1000L;
                List<String> additionalDetails = (st.get(i).getAdditionalDetails() != null) ? st.get(i).getAdditionalDetails() : Collections.emptyList();
                out.append(st.get(i).getSourceStep()).append("->").append(st.get(i).getDestinationStep()).append(": ").append(st.get(i).getAction())
                        .append(" ")
                        .append(additionalDetails)
                        .append(" Time spent: ")
                        .append(timeElapsed)
                        .append(" s.")
                        .append("\n");
            }
        }
        List<String> additionalDetails = st.get(steps.size() - 1).getAdditionalDetails() == null ? Collections.emptyList() : st.get(steps.size() - 1).getAdditionalDetails();
        out.append(st.get(steps.size() - 1).getSourceStep()).append("->").append(st.get(steps.size() - 1).getDestinationStep()).append(": ").append(st.get(steps.size() - 1).getAction())
                .append(" ")
                .append(additionalDetails)
                .append("\n");
        seq = out.toString();
        renderDiagram = true;
        Set<String> logFileNames = steps.stream().map(s -> s.getLogFile().substring(s.getLogFile().lastIndexOf(File.separator) + 1)).collect(Collectors.toSet());
        for (String logFile : logFileNames) {
            logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, 0, logFile, ipAddress);
            offsets.put(logFile, 1000);
            LogFile lf = new LogFile();
            lf.setFileName(logFile);
            lf.setLogs(logs);
            this.files.add(lf);
        }

    }

    private Connection createConnection(Element prev, Element curr, Step prevStep, Step currStep, int prevIndex, int currIndex) {
        Connection conn = new Connection(prev.getEndPoints().get(prevIndex), curr.getEndPoints().get(currIndex));
        return conn;
    }
}
