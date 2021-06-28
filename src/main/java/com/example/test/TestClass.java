package com.example.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

public class TestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestClass.class);

    public static final int waitingTime = 1000;
    private int i1;
    public double f1;
    private boolean b1;

    @Autowired
    private RestTemplate restTemplate;

    protected void a1(int x) throws InterruptedException  {
        a2("", "");
        add(4);
    }

    private String add(int x) {
        f1=x;
        return "" + i1;
    }

    public void a2(String name, @RequestParam("strVal") String value) throws InterruptedException {

        LOGGER.info("");
        LOGGER.info("test");

        Exception ex = new Exception("custom exception");
        LOGGER.error("my exception here", ex);
        LOGGER.info(name, value);
        LOGGER.info("Hello m1 param1 : {} param 2: {}", name, value);
    }

    public void m1(String name, @PathVariable Department value, ResponseEntity<Department> empRespEntity) {
        int i = 10;

        while (i < 4) {
            i++;
        }

        LOGGER.info(escapeXml(name));

        Employee emp1 = value.getEmployee();
        LOGGER.info("Employee name : {} {} {}", name, value, emp1.getName());

        Department dep = empRespEntity.getBody();
        Employee emp = dep.getEmployee();

        LOGGER.info("Employee name : {}", empRespEntity, dep, emp.getName());

        String fooResourceUrl = "https://www.google.co.in";

        LOGGER.info("fooResourceUrl : {}", fooResourceUrl);
        HttpEntity<Department> responseEntity = restTemplate.exchange(fooResourceUrl, HttpMethod.GET, getRequestEntity(), Department.class);
        //ResponseEntity<Department> responseEntity = restTemplate.getForEntity(fooResourceUrl, Department.class);
        //ResponseEntity<Department> responseEntity = restTemplate.getForEntity(fooResourceUrl, Department.class, getRequestEntity());
        //ResponseEntity<Department> responseEntity = restTemplate.postForEntity(fooResourceUrl, getRequestEntity(), Department.class);

        Department department = responseEntity.getBody();
        Employee employee = department.getEmployee();

        LOGGER.info("resp : {}", responseEntity);
        LOGGER.info("responseEntity.body.name : {}", responseEntity.getBody().getEmployee().getName());
        LOGGER.info("dep.emp.name : {} {} " + department.getEmployee().getName(), employee.getName(), department);
    }

    private HttpEntity<MultiValueMap<String, String>> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("id", "1");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return request;

        //ResponseEntity<String> response = restTemplate.getForEntity(fooResourceUrl, String.class);
    }


    void m2(Object obj) {
        LOGGER.info("Hello m2 {]", obj);
        /*

         */

        /*

         */
    }

    private String escapeXml(Object inputParam) {
        return inputParam.toString();
    }

    static void main(String[] args) {
        TestClass test = new TestClass();
    }
}