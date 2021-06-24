package distributed.cache.redis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import distributed.cache.redis.entity.*;
import distributed.cache.redis.lock.RedisLock;
import distributed.cache.redis.mapper.OmsOrderDetailMapper;
import distributed.cache.redis.mapper.OmsOrderMapper;
import distributed.cache.redis.service.OmsCartDetailService;
import distributed.cache.redis.service.OmsOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import distributed.cache.redis.service.PmsSkuStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@Slf4j
@Service
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OmsOrderService {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    OmsCartDetailService omsCartDetailService;
    @Autowired
    PmsSkuStockService pmsSkuStockService;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderDetailMapper omsOrderDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateOrder(UmsMember umsMember) throws Exception {
        //查询该用户加入购物车的所有商品信息
        List<OmsCartDetail> cartDetails = omsCartDetailService.queryCartDetailByMemberId(umsMember.getId());

        //根据购物车中商品下订单，计算总金额
        List<OmsOrderDetail> omsOrderDetails = new ArrayList<>();

        BigDecimal sum = new BigDecimal(0);
        for (OmsCartDetail ocd : cartDetails) {

            Long productId = ocd.getProductId();
            //使用redis分布式锁 锁库存
            RedisLock.lock(redisTemplate, "product_" + productId, String.valueOf(Thread.currentThread().getId()));
            //减库存
            PmsSkuStock pmsSkuStock = new PmsSkuStock();
            pmsSkuStock.setStock(-ocd.getQuantity());
            pmsSkuStock.setProductId(ocd.getProductId());
            int stockResult = pmsSkuStockService.updateProductStock(pmsSkuStock);
            if (stockResult == 0) {
                //未能锁定库存，直接释放分布式锁，并抛出异常
                RedisLock.unLock(redisTemplate, "product_" + productId, String.valueOf(Thread.currentThread().getId()));
                throw new Exception("商品售罄！" + ocd.getProductId() + " : " + ocd.getProductName());
            }
            //释放锁
            RedisLock.unLock(redisTemplate, "product_" + productId, String.valueOf(Thread.currentThread().getId()));

            //创建订单详情
            OmsOrderDetail omsOrderDetail = new OmsOrderDetail();
            omsOrderDetail.setMemberId(umsMember.getId());
            omsOrderDetail.setProductId(productId);
            omsOrderDetail.setProductName(ocd.getProductName());
            omsOrderDetail.setQuantity(ocd.getQuantity());
            omsOrderDetail.setRealPrice(ocd.getPrice());
            omsOrderDetails.add(omsOrderDetail);
            //计算总金额
            sum = sum.add(ocd.getPrice());
        }

        //创建订单信息
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setMemberId(umsMember.getId());
        omsOrder.setMemberUsername(umsMember.getUsername());
        omsOrder.setSourceType(2);
        omsOrder.setStatus(0);
        omsOrder.setTotalAmount(sum);
        omsOrder.setDeleteStatus(0);
        omsOrder.setOrderType(1);
        omsOrder.setPayType(1);
        omsOrder.setReceiverName(umsMember.getUsername());
        omsOrder.setReceiverPhone(umsMember.getTelephone());
        //生成订单，并获取订单号
        int orderResult = omsOrderMapper.insertOrder(omsOrder);

        Long orderId = omsOrder.getId();
        for (OmsOrderDetail ood : omsOrderDetails) {
            //设置订单详情订单号
            ood.setOrderId(orderId);
        }
        //添加订单详情
        int orderDetailResult = omsOrderDetailMapper.insertOrderDetailList(omsOrderDetails);

        //设置redis 订单超时时间，超时30分钟未支付自动取消订单，并释放商品，返回库存
        //这里为了测试，设置为1分钟
        redisTemplate.opsForValue().setIfAbsent("Order_" + omsOrder.getId(), omsOrder.getStatus(), Duration.ofMinutes(1));
        log.info("========== Set order key in redis: " + "Order_" + omsOrder.getId());
        return orderId;
    }

    @Override
    public Object queryOrderByMemberIdAndOrderId(long orderId) {
        QueryWrapper<OmsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", orderId);
        //queryWrapper.eq("member_id", memberId);
        return omsOrderMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelOrder(OmsOrder omsOrder) {
        return omsOrderMapper.cancelOrder(omsOrder);
    }

}
