package com.bo.loganalyzer.utils;

import com.bo.loganalyzer.exception.FileParsingException;
import com.bo.loganalyzer.model.Event;
import com.bo.loganalyzer.model.RequestType;
import com.bo.loganalyzer.model.Status;
import com.bo.loganalyzer.service.LogService;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileUtils {

    private static final String NEW_EVENT_REGEX = "^.*(\\d{4}-\\d{2}-\\d{2}) (\\d{2}:\\d{2}:\\d{2}).*$";
    private static final Pattern NEW_EVENT_PATTERN = Pattern.compile(NEW_EVENT_REGEX);

    private static final String IP_ADDR_REGEX = "(?s)(?<=IP-Address=).*?(?=#)";
    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(IP_ADDR_REGEX);

    private static final String ATTRIBUTES_REGEX = "(?s)(?<=!).*?(?=#)";
    private static final Pattern ATTRIBUTES_PATTERN = Pattern.compile(ATTRIBUTES_REGEX);

    @Autowired
    private LogService logService;

    public void parseFiles(MultipartFile file){
        LineIterator it = null;
        ExecutorService executorService = null;
        try {
            it = org.apache.commons.io.FileUtils.lineIterator(convert(file), "UTF-8");
            StringBuffer sb = new StringBuffer("");
            executorService = Executors.newFixedThreadPool(20);
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.matches(NEW_EVENT_REGEX)){
                    createAndPersistEvents(sb, executorService);
                    sb.delete(0, sb.length());
                }
                sb.append(line);
            }
            //last event
            createAndPersistEvents(sb, executorService);
        }catch (IOException e) {
            throw new FileParsingException(e.getMessage());
        }
        finally {
            try {
                it.close();
                executorService.shutdown();
            } catch (IOException e) {
                throw new FileParsingException(e.getMessage());
            }
        }
    }

    private void createAndPersistEvents(StringBuffer sb, ExecutorService executorService) {
        if (sb.length()>0){// if previous event exists then create object
            Event event = createEvents(sb);
            executorService.execute(new Runnable() {
                public void run() {
                   logService.saveEvents(event);
                }
            });
        }
    }

    private static Event createEvents(StringBuffer sb) {
        Matcher newEventMatcher = NEW_EVENT_PATTERN.matcher(sb);
        Event event = null;
        if (newEventMatcher.find()) {
            event = new Event();
            event.setLogTime(DateTimeFormatter.formatTime(newEventMatcher.group(1) + " " + newEventMatcher.group(2)));
            Matcher ipAddrMatcher = IP_ADDR_PATTERN.matcher(sb);
            if (ipAddrMatcher.find()){
                event.setIpAddress(ipAddrMatcher.group(0));
            }
            Matcher attributeMatcher = ATTRIBUTES_PATTERN.matcher(sb);
            while (attributeMatcher.find()){
                setEventAttributes(event,attributeMatcher.group(0));
            }
        }
        return event;
    }

    private static void setEventAttributes(Event event, String matchedChar) {
        String key = getKey(matchedChar);
        String value = getValue(matchedChar);
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value))
            return;
        switch (key){
            case "User-Agent":
                event.setUserAgent(value);
                break;
            case "X-Request-From":
                event.setRequestFrom(value);
                break;
            case "Request-Type":
                event.setRequestType(RequestType.getRequestType(value));
                break;
            case "API":
                event.setApi(value);
                break;
            case "User-Login":
                event.setUserLogin(value);
                break;
            case "User-Name":
                event.setUserName(value);
                break;
            case "EnterpriseId":
                event.setEnterpriseId(Long.valueOf(value));
                break;
            case "EnterpriseName":
                event.setEnterpriseName(value);
                break;
            case "Auth-Status":
                event.setAuthStatus(value);
                break;
            case "Status-Code":
                event.setStatus(Status.getStatus(Integer.valueOf(value)));
                break;
            case "Response-Time":
                event.setResponseTime(Long.valueOf(value));
        }
    }

    private static String getKey(String matchedChar) {
        String[] matchedCharArray = null;
        if (matchedChar.contains("=")) {
            matchedCharArray = matchedChar.split("=");
            if (matchedCharArray.length>0)
                return matchedCharArray[0];
        }
        return "";
    }

    private static String getValue(String matchedChar) {
        String[] matchedCharArray = null;
        if (matchedChar.contains("=")) {
            matchedCharArray = matchedChar.split("=");
            if (matchedCharArray.length>1)
                return matchedCharArray[1];
        }
        return "";
    }

    public static File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename()+"_"+DateTimeFormatter.getCurrentDateTime()
                .replace(" ", "_").replace(":", "_"));
        convFile.createNewFile();
        try(InputStream is = file.getInputStream()) {
            Files.copy(is, convFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return convFile;
    }
}
