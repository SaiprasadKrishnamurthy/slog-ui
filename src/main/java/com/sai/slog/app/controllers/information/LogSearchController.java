package com.sai.slog.app.controllers.information;

import com.sai.slog.app.model.Log;
import com.sai.slog.app.model.LogFile;
import com.sai.slog.app.model.Step;
import com.sai.slog.app.model.StepLookupIndex;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.component.gchart.model.GChartModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.Element;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
    private Date toDate = new Date();
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
    private String tag;
    private String logFileName;
    private String flowFunctionality;
    private String ipAddress;
    private int aroundMinutes = 1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'hh:mm:ss");
    private final Set<String> allLogFiles = new HashSet<>();
    private String bookmarkLink;

    {
        fromDate = new Date(System.currentTimeMillis() - (1000 * 60 * 60 *24 * 2));
    }


    public LogSearchController() {
        customers = logService.allCustomers();
        components = logService.allComponents();
        components.forEach(c -> allLogFiles.addAll(logService.logFileNames(c)));

        HttpServletRequest rq = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (StringUtils.isNotBlank(rq.getParameter("saved"))) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "You are viewing a shared search"));
            customer = rq.getParameter("customer");
            component = rq.getParameter("component");
            if (StringUtils.isNotBlank(rq.getParameter("from"))) {
                fromDate = new Date(Long.parseLong(rq.getParameter("from")));
            }
            if (StringUtils.isNotBlank(rq.getParameter("to"))) {
                toDate = new Date(Long.parseLong(rq.getParameter("to")));
            }
            logLevel = rq.getParameter("logLevel");
            ipAddress = rq.getParameter("ipAddress");
            messageFreeText = rq.getParameter("messageFreeText");
            logFileName = rq.getParameter("fileName");
            tag = rq.getParameter("tags");
            search();
        }
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

        // Don't include the ones that are already in the view.
        List<String> otherLogs = allLogFiles.stream().filter(lf -> !this.files.stream().anyMatch(l -> l.getFileName().equals(lf))).distinct().collect(Collectors.toList());


        System.out.println("Other log files: " + otherLogs);
        for (String logFile : otherLogs) {
            logs = logService.logsSearch(customer, null, null, fromDate, toDate, null, 0, logFile, ipAddress, tag);
            if (!logs.isEmpty()) {
                offsets.put(logFile, 500);
                System.out.println("\t\tLogfile: " + logFile);
                System.out.println("\t\tLogfile Size: " + logs.size());
                LogFile lf = new LogFile();
                lf.setFileName(logFile);
                lf.setLogs(logs);
                this.files.add(lf);
            }
        }
        searchResultsFound = true;
    }

    public void search() {
        this.files.clear();
        List<String> logFileNames = logService.logFileNames(component);
        if (StringUtils.isNotBlank(logFileName)) {
            logFileNames.clear();
            logFileNames.add(logFileName);
            component = null;
        }

        for (String logFile : logFileNames) {
            logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, 0, logFile, ipAddress, tag);
            offsets.put(logFile, 500);
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
        start = start - 500;
        logFile.setStartRecord((start <= 0) ? 0 : start);
        logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, logFile.getStartRecord(), logFile.getFileName(), ipAddress, tag);
        logFile.setLogs(logs);
        logFileTabIndex = IntStream.range(0, files.size()).filter(i -> files.get(i).getFileName().equals(_fileName)).findFirst().getAsInt();
    }

    public void nextSet() {
        String _fileName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("fileName");
        LogFile logFile = files.stream().filter(lf -> lf.getFileName().equals(_fileName)).findFirst().get();
        int start = logFile.getStartRecord();
        start = start + 500;
        logFile.setStartRecord((start < 0) ? 0 : start);
        logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, logFile.getStartRecord(), logFile.getFileName(), ipAddress, tag);
        logFile.setLogs(logs);
        logFileTabIndex = IntStream.range(0, files.size()).filter(i -> files.get(i).getFileName().equals(_fileName)).findFirst().getAsInt();
    }

    public void bookmark() throws Exception {
        StringBuilder out = new StringBuilder();
        append("customer", customer, out);
        append("component", component, out);
        if (fromDate != null) {
            append("from", fromDate.getTime() + "", out);
        }
        if (toDate != null) {
            append("to", toDate.getTime() + "", out);
        }
        append("logLevel", logLevel, out);
        append("ipAddress", ipAddress, out);
        append("messageFreeText", messageFreeText, out);
        append("tags", tag, out);
        append("fileName", logFileName, out);

        String url = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURL().toString();
        this.bookmarkLink = url + "?" + out.toString() + "saved=true";
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Bookmark link", this.bookmarkLink);
        RequestContext.getCurrentInstance().showMessageInDialog(message);

    }

    private void append(String name, String value, StringBuilder out) {
        if (StringUtils.isNotBlank(value)) {
            out.append(name + "=" + value).append("&");
        }
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
            logs = logService.logsSearch(customer, component, logLevel, fromDate, toDate, messageFreeText, 0, logFile, ipAddress, tag);
            offsets.put(logFile, 500);
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

    public static byte[] compress(String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }

    public static String decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }
}
