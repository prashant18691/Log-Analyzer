package com.bo.loganalyzer.dao;

import com.bo.loganalyzer.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogDao extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
}
