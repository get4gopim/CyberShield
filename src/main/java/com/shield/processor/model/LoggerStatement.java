package com.shield.processor.model;

import java.util.List;

public class LoggerStatement {

    /**
     * This string builder has logger variable name and trace level.
     * Ex: LOGGER.info, LOG.debug, logger.error
     */
    private StringBuilder loggerStmt;

    /**
     * This list has the logger params used inside the log statement.
     * Ex: LOGGER.info("my message", name, value.getVal()); -> "my message", name, value.getVal() will be stored in the List.
     */
    private List<LoggerParam> loggerParams;

    /**
     * This indicator will informs whether modification/sanitization is needed or not for the entire log statement.
     */
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
