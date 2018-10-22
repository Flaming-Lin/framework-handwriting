package com.flaming.client.controller;

import com.flaming.client.service.IndexService;
import com.flaming.spring.annotation.MyAutowired;
import com.flaming.spring.annotation.MyController;
import com.flaming.spring.annotation.MyRequestMapping;
import com.flaming.spring.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author Flaming
 * @date 2018/10/22 12:24
 */
@MyController
@MyRequestMapping("/index")
public class IndexController {

    @MyAutowired
    private IndexService indexService;

    @MyRequestMapping("/find")
    public void find(HttpServletRequest request, HttpServletResponse response,
                     @MyRequestParam("name") String name) {
        String printName = indexService.get(name);
        try {
            PrintWriter pw = response.getWriter();
            pw.print(printName);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @MyRequestMapping("/add")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @MyRequestParam("a") Integer a, @MyRequestParam("b") Integer b) {

    }

}
