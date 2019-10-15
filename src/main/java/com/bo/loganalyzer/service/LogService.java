package com.bo.loganalyzer.service;

import com.bo.loganalyzer.dao.LogDao;
import com.bo.loganalyzer.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    @Autowired
    private LogDao logDao;

    public Page<Event> retrieveAllEvents(int page, int size){
        return logDao.findAll(PageRequest.of(page, size, Sort.by("logTime")));
    }

    public Optional<Event> getEventsById(long id){
        return logDao.findById(id);
    }

    public void saveEvents(Event event){
        logger.info("Entered saveEvents");
        logDao.save(event);
    }

    public Page<Event> filterEvents(Specification<Event> specs,int page, int size){
        return logDao.findAll(specs,PageRequest.of(page, size, Sort.by("logTime")));
    }
}
