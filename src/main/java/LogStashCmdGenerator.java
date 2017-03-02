import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by prabvara on 2/28/2017.
 */
public class LogStashCmdGenerator {
    String path = "/slog/logs/2_hr_sanity_tests/server1/77.695184_12.937164/";
    public static void main(String[] args) {  new LogStashCmdGenerator().findMatchingFiles();}

//Read property file for log file config and corresponding  file name RegEx patterns
    public Map<String, String> fetchPropertyFromFile()
    {
        Map<String, String> components = new HashMap<>();
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream("/slog/properties/LogAnalyzer.properties");
            props.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
// Add component config , logname RegEx pattern to a Map. This map will be used to search matching files and forming logstash command
        for (String key : props .stringPropertyNames())
        {
            System.out.println(key + " = " + props .getProperty(key));
            components.put (key, props.getProperty(key));
        }

        return components;
    }

//Get names of all log files present in un-tared logs directory. Filter out .tar , .zip , .gz etc.,
    public List getAllLogFileNames() {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        List<String> fileList = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();
                if ((!fileName.contains(".tar")) && (!fileName.contains(".zip")) && (!fileName.contains(".gz")) && (!fileName.contains(".lck")))
                    fileList.add(fileName);
            }
        }
        return fileList;
    }
// Find matching logs from logs directory

    public void findMatchingFiles()
    {
		
		//Get Server name from the log directory path
						
		String server = path.substring(0, path.length()-2);
		server = server.substring(0,server.lastIndexOf("/"));
		server = server.substring(server.lastIndexOf("/") + 1);
		System.out.println("Server Name is : " + server);
						
        Map <String, String> logPatternMap = new HashMap<>(); // Map of log stash config and RegEx
        List<String> fileList = new ArrayList<>(); // List of All files in Log Dir
        List<String> logStashCommandList = new ArrayList<>(); // List of commands to be executed for log stashing files
        String placeholder = "FILE_PATH";
        logPatternMap = fetchPropertyFromFile();
        fileList = getAllLogFileNames();

        for (String file : fileList) {

            for (Map.Entry<String, String> entry : logPatternMap.entrySet()) {
                String fileName = entry.getKey();
                String pattern = entry.getValue();

                if (Pattern.matches(pattern,file))
                {
                    System.out.println( "Found a matching file : " + pattern + " : " + file );
                    File source = new File("/slog/templates/" + fileName);
                    File dest = new File("/slog/configs/" + fileName);
                    File logFile = new File(path + file);
                    try {
						
						
                        //copy files from template directory to configs directory
						Files.copy(source.toPath(), dest.toPath()); 
                        System.out.println("Destination path is : " + dest.getPath());

                        //Replace contents of the config file to match log file paths

                        BufferedReader reader = new BufferedReader(new FileReader(dest));
                        String line = "", oldtext = "";
                        while((line = reader.readLine()) != null)
                        {
                            oldtext += line + "\r\n";
                        }
                        reader.close();

                         String replacedtext  = oldtext.replaceAll(placeholder, logFile.getPath().toString());

                        FileWriter writer = new FileWriter(dest);
                        writer.write(replacedtext);
                        writer.close();

                        //Form LogStash command and Execute it

                        String command = "/slog/bin/logstash -f " + dest.getPath() + " < " + path + file;
                        String[] cmd = { "/bin/sh", "-c", command};
                        Process process = Runtime.getRuntime().exec(cmd);
                        try {
                            BufferedReader out = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                            out.lines().forEach(System.out::println);
                            process.waitFor();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //delete config file and log file after the work is done
                        dest.delete();
                        // logFile.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }
}