package com.nerotomato.shardingspherejdbc.service;

import com.nerotomato.shardingspherejdbc.entity.UmsMember;
import com.nerotomato.shardingspherejdbc.entity.PageRequest;
import com.nerotomato.shardingspherejdbc.entity.PageResult;

/**
 * 会员用户Service
 * Created by nero on 2021/4/28.
 */
public interface UmsMemberService extends BaseService<UmsMember> {

    /**
     * 查询分页数据
     *
     * @return
     */
    public PageResult findByPage(PageRequest pageRequest);
}
