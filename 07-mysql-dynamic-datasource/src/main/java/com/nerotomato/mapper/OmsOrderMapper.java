package com.nerotomato.mapper;

import com.nerotomato.entity.OmsOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by nero on 2021/5/11.
 */
@Mapper
public interface OmsOrderMapper {
    /**
     * 新增订单
     *
     * @param order
     * @return
     */
    public int insertOrder(OmsOrder order);

    /**
     * 删除订单
     *
     * @param order
     */
    public int deleteOrderByOrderSn(OmsOrder order);

    /**
     * 查询全部订单
     *
     * @return
     */
    public List<OmsOrder> queryAllOrders();

    /**
     * 查询分页数据
     *
     * @return
     */
    public List<OmsOrder> queryOrdersByPage();


    /**
     * 更新订单
     */
    int updateOrderByOrderSn(OmsOrder order);

    OmsOrder queryOrderByOrderSn(OmsOrder order);

    int batchSaveOrder(List<OmsOrder> omsOrderList);
}
