package com.nerotomato.shardingsphere.proxy.service;

import com.nerotomato.shardingsphere.proxy.entity.PageRequest;
import com.nerotomato.shardingsphere.proxy.entity.PageResult;
import com.nerotomato.shardingsphere.proxy.entity.UmsMember;

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
