package com.nerotomato.shardingspherejdbc.utils;

import com.github.pagehelper.PageInfo;
import com.nerotomato.shardingspherejdbc.entity.PageResult;

/**
 * Created by nero on 2021/4/30.
 */
public class PageUtils {
    /**
     * 将分页信息封装到统一的接口
     *
     * @param pageInfo
     * @return
     */
    public static PageResult getPageResult(PageInfo<?> pageInfo) {
        PageResult pageResult = new PageResult();
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setTotalSize(pageInfo.getTotal());
        pageResult.setTotalPages(pageInfo.getPages());
        pageResult.setContent(pageInfo.getList());
        return pageResult;
    }
}
