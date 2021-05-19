package com.nerotomato.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nerotomato.datasource.annotation.DataSourceRouting;
import com.nerotomato.datasource.type.DynamicDataSource;
import com.nerotomato.entity.*;
import com.nerotomato.mapper.OmsOrderMapper;
import com.nerotomato.service.OmsOrderService;
import com.nerotomato.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nero on 2021/5/12.
 */
@Service
public class OmsOrderServiceImpl implements OmsOrderService {

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public int save(OmsOrder order) {
        return omsOrderMapper.insertOrder(order);
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public int delete(OmsOrder order) {
        return omsOrderMapper.deleteOrderByOrderSn(order);
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public int update(OmsOrder order) {
        return omsOrderMapper.updateOrderByOrderSn(order);
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.SLAVE)
    public OmsOrder find(OmsOrder order) {
        return omsOrderMapper.queryOrderByOrderSn(order);
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.SLAVE)
    public List<OmsOrder> findAll() {
        return omsOrderMapper.queryAllOrders();
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
    private PageInfo<OmsOrder> getPageInfo(PageRequest pageRequest) {
        int pageNum = pageRequest.getPage();
        int pageSize = pageRequest.getSize();
        PageHelper.startPage(pageNum, pageSize);
        List<OmsOrder> omsOrderList = omsOrderMapper.queryOrdersByPage();
        return new PageInfo<OmsOrder>(omsOrderList);
    }

    /**
     * 1-for循环方式
     * 批量新增订单
     */
    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public Object saveMultiOrders(int nums) {
        int count = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < nums; i++) {
            OmsOrder order = new OmsOrder();
            order.setOrderSn(i + "");
            order.setMemberId(1);
            order.setMemberUsername("nero");
            order.setOrderType(0);
            order.setPayType(1);
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            order.setConfirmStatus(0);
            order.setDeleteStatus(0);
            order.setNote("测试订单" + i);
            order.setReceiverCity("Shanghai");
            order.setReceiverName("尼禄");
            order.setReceiverPhone("17621504249");
            order.setReceiverProvince("Shanghai");
            order.setReceiverPostCode("200120");
            order.setReceiverRegion("浦东新区");
            order.setReceiverDetailAddress("浦东新区");
            //order.setPaymentTime(new Date());
            //order.setCommentTime(new Date());
            omsOrderMapper.insertOrder(order);
            count++;

        }
        long end = System.currentTimeMillis();
        System.out.println(count + " 笔订单共耗时: " + (end - start) + " 毫秒");
        System.out.println(count + " 笔订单共耗时: " + ((end - start) / 1000) + " 秒");
        return count;
    }

    @Override
    @DataSourceRouting(value = DynamicDataSource.MASTER)
    public Object batchSaveOrders(int nums) {
        int sum = 0;
        int count = 0;
        List<OmsOrder> omsOrders = new ArrayList<>();
        long start = System.currentTimeMillis();
        int times1 = nums / 10000;
        int times2 = nums % 10000;
        for (int i = 0; i < (times1 * 10000); i++) {
            OmsOrder order = new OmsOrder();
            order.setOrderSn(i + "");
            order.setMemberId(1);
            order.setMemberUsername("nero");
            order.setOrderType(0);
            order.setPayType(1);
            order.setTotalAmount(new BigDecimal(9999.00));
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            order.setConfirmStatus(0);
            order.setDeleteStatus(0);
            order.setNote("测试订单" + i);
            order.setReceiverCity("Shanghai");
            order.setReceiverName("尼禄");
            order.setReceiverPhone("17621504249");
            order.setReceiverProvince("Shanghai");
            order.setReceiverPostCode("200120");
            order.setReceiverRegion("浦东新区");
            order.setReceiverDetailAddress("浦东新区");
            omsOrders.add(order);
            count++;
            //每次只插入10000条数据
            if (count == 10000) {
                int result = omsOrderMapper.batchSaveOrder(omsOrders);
                omsOrders.clear();
                sum += count;
                count = 0;
            }
        }
        omsOrders.clear();
        if (times2 != 0) {
            for (int i = 0; i < times2; i++) {
                OmsOrder order = new OmsOrder();
                order.setOrderSn(i + "");
                order.setMemberId(1);
                order.setMemberUsername("nero");
                order.setOrderType(0);
                order.setPayType(1);
                order.setTotalAmount(new BigDecimal(9999.00));
                order.setCreateTime(new Date());
                order.setUpdateTime(new Date());
                order.setConfirmStatus(0);
                order.setDeleteStatus(0);
                order.setNote("测试订单" + i);
                order.setReceiverCity("Shanghai");
                order.setReceiverName("尼禄");
                order.setReceiverPhone("17621504249");
                order.setReceiverProvince("Shanghai");
                order.setReceiverPostCode("200120");
                order.setReceiverRegion("浦东新区");
                order.setReceiverDetailAddress("浦东新区");
                omsOrders.add(order);
                count++;
            }
            //集合omsOrders的size大小不能为0,否则批处理执行会报错
            int result = omsOrderMapper.batchSaveOrder(omsOrders);
            sum += count;
        }

        long end = System.currentTimeMillis();
        System.out.println(sum + " 笔订单共耗时: " + (end - start) + " 毫秒");
        System.out.println(sum + " 笔订单共耗时: " + ((end - start) / 1000) + " 秒");
        return "总数 : " + sum;
    }
}
