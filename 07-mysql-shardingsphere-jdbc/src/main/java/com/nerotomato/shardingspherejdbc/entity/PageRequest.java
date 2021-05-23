package com.nerotomato.shardingspherejdbc.entity;

import lombok.Data;

/**
 *
 * 分页请求类
 *
 * Created by nero on 2021/4/28.
 */
@Data
public class PageRequest {
    private int page;
    private int size;
}
