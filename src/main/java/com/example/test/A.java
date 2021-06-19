package com.example.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;

public class A {

    private static final Logger LOGGER = LoggerFactory.getLogger(A.class);

    public static final int waitingTime = 1000;
    private int i1;
    public double f1;
    private boolean b1;

    protected void a1(int x) throws InterruptedException  {
        a2();
        add(4);
    }

    private String add(int x) {
        f1=x;
        return "" + i1;
    }

    protected void a2() throws InterruptedException {
        a2();
    }

    public void m1(String name, @PathVariable String value) {
        int i = 10;

        new B().show(i);

        while (i < 4) {
            i++;
        }

        LOGGER.info("");
        LOGGER.info("test");
        LOGGER.info(escapeXml(name));
        Exception ex = new Exception("custom exception");
        LOGGER.error("my exception here", ex);
        LOGGER.info(name, value);
        LOGGER.info("Hello m1 param1 : {} param 2: {}", name, value);

    }

    void m2(Object obj) {
        LOGGER.info("Hello m2 {]", obj);
    }

    private String escapeXml(Object inputParam) {
        return inputParam.toString();
    }
}