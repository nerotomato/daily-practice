package com.nerotomato.shardingspherejdbc.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nerotomato.shardingspherejdbc.entity.OmsOrder;
import com.nerotomato.shardingspherejdbc.entity.PageRequest;
import com.nerotomato.shardingspherejdbc.mapper.OmsOrderMapper;
import com.nerotomato.shardingspherejdbc.service.OmsOrderService;
import com.nerotomato.shardingspherejdbc.service.RedisService;
import com.nerotomato.shardingspherejdbc.entity.PageResult;
import com.nerotomato.shardingspherejdbc.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nero on 2021/5/12.
 */
@Service
public class OmsOrderServiceImpl implements OmsOrderService {

    @Value("${spring.redis.key.orderId}")
    private String REDIS_KEY_ORDER_ID;
    @Value("${spring.redis.key.database}")
    private String REDIS_DATABASE;
    @Autowired
    RedisService redisService;
    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Override
    public int save(OmsOrder order) {
        order.setOrderSn(generateOrderSn(order));
        return omsOrderMapper.insertOrder(order);
    }

    @Override
    public int delete(OmsOrder order) {
        return omsOrderMapper.deleteOrderByOrderSn(order);
    }

    @Override
    public int update(OmsOrder order) {
        return omsOrderMapper.updateOrderByOrderSn(order);
    }

    @Override
    public OmsOrder find(OmsOrder order) {
        return omsOrderMapper.queryOrderByOrderSn(order);
    }

    @Override
    public List<OmsOrder> findAll() {
        return omsOrderMapper.queryAllOrders();
    }

    @Override
    public PageResult findByPage(PageRequest pageRequest) {
        return PageUtils.getPageResult(getPageInfo(pageRequest));
    }

    /**
     * ??????????????????????????????
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
     * 1-for????????????
     * ??????????????????
     */
    @Override
    public Object saveMultiOrders(int nums) {
        int count = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < nums; i++) {
            OmsOrder order = new OmsOrder();
            order.setMemberId(1l);
            order.setSourceType(2);
            order.setMemberUsername("nero");
            order.setOrderType(0);
            order.setPayType(1);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setConfirmStatus(0);
            order.setDeleteStatus(0);
            order.setNote("????????????" + i);
            order.setReceiverCity("Shanghai");
            order.setReceiverName("??????");
            order.setReceiverPhone("17621504249");
            order.setReceiverProvince("Shanghai");
            order.setReceiverPostCode("200120");
            order.setReceiverRegion("????????????");
            order.setReceiverDetailAddress("????????????");
            order.setOrderSn(generateOrderSn(order));
            omsOrderMapper.insertOrder(order);
            count++;

        }
        long end = System.currentTimeMillis();
        System.out.println(count + " ??????????????????: " + (end - start) + " ??????");
        System.out.println(count + " ??????????????????: " + ((end - start) / 1000) + " ???");
        return count;
    }

    @Override
    public Object batchSaveOrders(int nums) {
        int sum = 0;
        int count = 0;
        List<OmsOrder> omsOrders = new ArrayList<>();
        long start = System.currentTimeMillis();
        int times1 = nums / 10000;
        int times2 = nums % 10000;
        for (int i = 0; i < (times1 * 10000); i++) {
            OmsOrder order = new OmsOrder();
            order.setSourceType(2);
            order.setMemberId(1l);
            order.setMemberUsername("nero");
            order.setOrderType(0);
            order.setPayType(1);
            order.setTotalAmount(new BigDecimal(9999.00));
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setConfirmStatus(0);
            order.setDeleteStatus(0);
            order.setNote("????????????" + i);
            order.setReceiverCity("Shanghai");
            order.setReceiverName("??????");
            order.setReceiverPhone("17621504249");
            order.setReceiverProvince("Shanghai");
            order.setReceiverPostCode("200120");
            order.setReceiverRegion("????????????");
            order.setReceiverDetailAddress("????????????");
            order.setOrderSn(generateOrderSn(order));
            omsOrders.add(order);
            count++;
            //???????????????10000?????????
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
                order.setSourceType(2);
                order.setMemberId(1l);
                order.setMemberUsername("nero");
                order.setOrderType(0);
                order.setPayType(1);
                order.setTotalAmount(new BigDecimal(9999.00));
                order.setCreateTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                order.setConfirmStatus(0);
                order.setDeleteStatus(0);
                order.setNote("????????????" + i);
                order.setReceiverCity("Shanghai");
                order.setReceiverName("??????");
                order.setReceiverPhone("17621504249");
                order.setReceiverProvince("Shanghai");
                order.setReceiverPostCode("200120");
                order.setReceiverRegion("????????????");
                order.setReceiverDetailAddress("????????????");
                order.setOrderSn(generateOrderSn(order));
                omsOrders.add(order);
                count++;
            }
            //??????omsOrders???size???????????????0,??????????????????????????????
            int result = omsOrderMapper.batchSaveOrder(omsOrders);
            sum += count;
        }

        long end = System.currentTimeMillis();
        System.out.println(sum + " ??????????????????: " + (end - start) + " ??????");
        System.out.println(sum + " ??????????????????: " + ((end - start) / 1000) + " ???");
        return "?????? : " + sum;
    }

    /**
     * ??????16????????????
     * 2???????????????+??????ID???4???+6?????????ID+????????????4???
     */
    private String generateOrderSn(OmsOrder order) {
        long memberId = order.getMemberId();

        StringBuilder sb = new StringBuilder();
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
        String memberIdStr = String.valueOf(memberId);
        String lastFourNumberMemberId = null;
        if (memberIdStr.length() < 4) {
            lastFourNumberMemberId = String.format("%04d", memberId);
        } else {
            lastFourNumberMemberId = memberIdStr.substring(memberIdStr.length() - 4);
        }

        Long increment = redisService.increment(key, 1);
        String currentTime = String.valueOf(new Date().getTime());
        String lastFourNumberCurrentTime = currentTime.substring(currentTime.length() - 4);

        sb.append(String.format("%02d", order.getSourceType()));
        sb.append(lastFourNumberMemberId);
        String incrementStr = increment.toString();
        if (incrementStr.length() <= 6) {
            sb.append(String.format("%06d", increment));
        } else {
            sb.append(incrementStr);
        }
        sb.append(lastFourNumberCurrentTime);
        return sb.toString();
    }

    public static void main(String[] args) {
        long memberId = 12345;
        String memberIdStr = String.valueOf(memberId);
        String lastFourNumberMemberId = null;
        if (memberIdStr.length() < 4) {
            lastFourNumberMemberId = String.format("%06d", memberId);
        } else {
            lastFourNumberMemberId = memberIdStr.substring(memberIdStr.length() - 4);
        }
        System.out.println(lastFourNumberMemberId);
    }
}
