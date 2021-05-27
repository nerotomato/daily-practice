package com.nerotomato.separate.service;



import com.nerotomato.separate.entity.PageRequest;
import com.nerotomato.separate.entity.PageResult;

import java.util.List;

/**
 * Created by nero on 2021/5/6.
 */
public interface BaseService<T> {

    /**
     * 保存
     *
     * @param object
     * @return
     */
    public int save(T object);

    /**
     * 删除
     *
     * @param object
     */
    public int delete(T object);

    /**
     * 更新
     *
     * @param object
     */
    public int update(T object);

    /**
     * 根据条件查询信息
     *
     * @param object
     */
    public T find(T object);

    /**
     * 查询全部信息
     *
     * @return
     */
    public List<T> findAll();

    /**
     * 查询分页信息
     *
     * @return
     */

    /**
     * 分页查询接口
     * 这里统一封装了分页请求和结果，避免直接引入具体框架的分页对象, 如MyBatis或JPA的分页对象
     * 从而避免因为替换ORM框架而导致服务层、控制层的分页接口也需要变动的情况，替换ORM框架也不会
     * 影响服务层以上的分页接口，起到了解耦的作用
     *
     * @param pageRequest 自定义，统一分页查询请求
     * @return PageResult 自定义，统一分页查询结果
     * @return
     */
    public PageResult findByPage(PageRequest pageRequest);
}
