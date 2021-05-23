package com.nerotomato.shardingsphere.proxy.service;

import com.nerotomato.shardingsphere.proxy.entity.OmsOrder;

/**
 * Created by nero on 2021/5/12.
 */
public interface OmsOrderService extends BaseService<OmsOrder> {
    Object saveMultiOrders(int nums);

    Object batchSaveOrders(int nums);
}
