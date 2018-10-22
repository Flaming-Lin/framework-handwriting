package com.flaming.client.service.impl;

import com.flaming.client.service.IndexService;
import com.flaming.spring.annotation.MyService;

/**
 * @Author Flaming
 * @date 2018/10/22 12:30
 */
@MyService
public class IndexServiceImpl implements IndexService {

    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
