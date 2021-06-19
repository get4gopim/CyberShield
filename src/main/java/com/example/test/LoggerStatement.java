package com.example.test;

import java.util.List;

public class LoggerStatement {

    private StringBuilder loggerStmt;
    private List<LoggerParam> loggerParams;
    private boolean isModificationNeeded;

    public LoggerStatement(StringBuilder loggerStmt, List<LoggerParam> loggerParams, boolean isModificationNeeded) {
        this.loggerStmt = loggerStmt;
        this.loggerParams = loggerParams;
        this.isModificationNeeded = isModificationNeeded;
    }

    public StringBuilder getLoggerStmt() {
        return loggerStmt;
    }

    public List<LoggerParam> getLoggerParams() {
        return loggerParams;
    }

    public boolean isModificationNeeded() {
        return isModificationNeeded;
    }
}
