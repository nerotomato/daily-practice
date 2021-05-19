package com.nerotomato.entity;

import lombok.Data;

/**
 * 数据库分页实体类
 * Created by nero on 2021/4/28.
 */
@Data
public class PageQuery {
    private int page;
    private int size;
}
