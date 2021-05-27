package com.nerotomato.separate.service;

import com.nerotomato.separate.entity.OmsOrder;

/**
 * Created by nero on 2021/5/12.
 */
public interface OmsOrderService extends BaseService<OmsOrder> {
    Object saveMultiOrders(int nums,long memberId);

    Object batchSaveOrders(int nums, long memberId);
}
