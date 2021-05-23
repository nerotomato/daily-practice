package com.nerotomato.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nerotomato.datasource.annotation.DataSourceRouting;
import com.nerotomato.datasource.type.DynamicDataSource;
import com.nerotomato.entity.*;
import com.nerotomato.mapper.UmsMemberMapper;
import com.nerotomato.service.UmsMemberService;
import com.nerotomato.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员用户Service实现类
 * Created by nero on 2021/4/28.
 */
@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    //注入dao数据库持久化操作类
    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public int save(UmsMember user) {
        return umsMemberMapper.insertMember(user);
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public int delete(UmsMember user) {
        umsMemberMapper.deleteMemberbyUsername(user);
        return 0;
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public int update(UmsMember object) {
        return 0;
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.SLAVE)
    public UmsMember find(UmsMember object) {
        return null;
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.SLAVE)
    public List<UmsMember> findAll() {
        return umsMemberMapper.queryAllMembers();
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.SLAVE)
    public PageResult findByPage(PageRequest pageRequest) {
        return PageUtils.getPageResult(getPageInfo(pageRequest));
    }

    /**
     * 调用分页插件完成分页
     *
     * @param pageRequest
     * @return
     */
    private PageInfo<UmsMember> getPageInfo(com.nerotomato.entity.PageRequest pageRequest) {
        int pageNum = pageRequest.getPage();
        int pageSize = pageRequest.getSize();
        PageHelper.startPage(pageNum, pageSize);
        List<UmsMember> umsMemberList = umsMemberMapper.queryMembersByPage();
        return new PageInfo<UmsMember>(umsMemberList);
    }
}
