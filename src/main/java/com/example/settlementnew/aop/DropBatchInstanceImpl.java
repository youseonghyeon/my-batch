package com.example.settlementnew.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class DropBatchInstanceImpl {

    private final JdbcTemplate jdbcTemplate;

    @Before("@annotation(com.example.settlementnew.aop.DropBatchInstance)")
    public void beforeJobTest() throws Throwable {
        log.info("Batch 스키마 삭제");
        executeScript("schema-drop-mysql.sql");
        log.info("Batch 스키마 생성");
        executeScript("schema-mysql.sql");
    }

    private void executeScript(String filePath) throws SQLException {
        ClassPathResource resource = new ClassPathResource(filePath);
        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), resource);
    }
}
