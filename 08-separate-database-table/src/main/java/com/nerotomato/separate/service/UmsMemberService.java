package com.nerotomato.separate.service;

import com.nerotomato.separate.entity.PageRequest;
import com.nerotomato.separate.entity.PageResult;
import com.nerotomato.separate.entity.UmsMember;

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
