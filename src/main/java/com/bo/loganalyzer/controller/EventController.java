package com.bo.loganalyzer.controller;

import com.bo.loganalyzer.exception.EventNotFoundException;
import com.bo.loganalyzer.exception.InvalidSearchParameterException;
import com.bo.loganalyzer.filter.EventSpecificationsBuilder;
import com.bo.loganalyzer.filter.SearchOperation;
import com.bo.loganalyzer.model.Event;
import com.bo.loganalyzer.service.LogService;
import com.bo.loganalyzer.utils.FileUtils;
import com.google.common.base.Joiner;
import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("logs-analyzer")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private static final String IP_ADDR_REGEX = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    private static final String EMAIL_ADDR_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}$";

    private static final String API_ENDPOINT_REGEX = "\\/\\w+(\\/\\w+)*";

    @Autowired
    private LogService logService;

    @Autowired
    private FileUtils fileUtils;

    @Throttling(type = ThrottlingType.RemoteAddr, limit = 1, timeUnit = TimeUnit.SECONDS)
    @PostMapping(path = "/upload")
    public ResponseEntity<Object> parseLogs(@RequestParam MultipartFile file){
        fileUtils.parseFiles(file);
        return new ResponseEntity<>("Logs created successfully",
                HttpStatus.OK);
    }
    @Throttling(type = ThrottlingType.RemoteAddr, limit = 1, timeUnit = TimeUnit.MILLISECONDS)
    @GetMapping("logs/{id}")
    public ResponseEntity<Object> getEventsById(@PathVariable long id){
        Optional<Event> optionalEvent = logService.getEventsById(id);
        if (!optionalEvent.isPresent())
            throw new EventNotFoundException("No log event with id : "+id+"found");
        Resource<Event> resource = new Resource<Event>(optionalEvent.get());
        ControllerLinkBuilder linkTo = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(
                this.getClass()).retrieveAllLogs(0,10));
        resource.add(linkTo.withRel("all-events"));
        return ResponseEntity.ok(resource);
    }
    @Throttling(type = ThrottlingType.RemoteAddr, limit = 1, timeUnit = TimeUnit.SECONDS)
    @GetMapping("/logs")
    public ResponseEntity<Object> retrieveAllLogs(@RequestParam(required = false, defaultValue = "0") int page,
                                                  @RequestParam(required = false, defaultValue = "10") int size){

        Page<Event> pageEvent = logService.retrieveAllEvents(page, size);
        List<Event> events = pageEvent.getContent();
        if (CollectionUtils.isEmpty(events)){
            throw new EventNotFoundException("No log events found");
        }
        return ResponseEntity.ok(events);
    }
    @Throttling(type = ThrottlingType.RemoteAddr, limit = 1, timeUnit = TimeUnit.SECONDS)
    @GetMapping("/filter-logs")
    public ResponseEntity<Object> filteredLogs(@RequestParam(required = false, defaultValue = "0") int page,
                                               @RequestParam(required = false, defaultValue = "100") int size,
                                               @RequestParam(required = false, defaultValue = "") String search){
        Specification<Event> specs = resolveSpecification(search);
        Page<Event> pageEvent = null;
        try {
            pageEvent = logService.filterEvents(specs, page, size);
        }
        catch (InvalidDataAccessApiUsageException ex){
            throw new InvalidSearchParameterException("Invalid search key in "+search);
        }
        List<Event> events = pageEvent.getContent();
        if (CollectionUtils.isEmpty(events)){
            throw new EventNotFoundException("No log events found");
        }
        return ResponseEntity.ok(events);
    }

    Specification<Event> resolveSpecification(String searchParameters) {

        EventSpecificationsBuilder builder = new EventSpecificationsBuilder();
        String operationSetExper = Joiner.on("|")
                .join(SearchOperation.SIMPLE_OPERATION_SET);
        Pattern pattern = Pattern.compile("(\\p{Punct}?)(\\w+?)(" + operationSetExper + ")(\\p{Punct}?)(\\w+?)(\\p{Punct}?),");
        Matcher matcher = pattern.matcher(searchParameters + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(5), matcher.group(4), matcher.group(6));
        }
        return builder.build();
    }


}
