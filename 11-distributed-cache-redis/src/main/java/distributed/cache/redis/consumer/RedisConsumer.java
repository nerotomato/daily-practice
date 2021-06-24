package distributed.cache.redis.consumer;

import distributed.cache.redis.entity.OmsOrder;
import distributed.cache.redis.entity.OmsOrderDetail;
import distributed.cache.redis.service.OmsOrderDetailService;
import distributed.cache.redis.service.OmsOrderService;
import distributed.cache.redis.service.PmsSkuStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RedisConsumer {
    @Autowired
    OmsOrderService omsOrderService;
    @Autowired
    OmsOrderDetailService omsOrderDetailService;
    @Autowired
    PmsSkuStockService pmsSkuStockService;

    /**
     * redis订阅推送的消息 会回调该方法  message：推送的信息
     */
    public void receiveMessage(String message) {
        //根据订单编号取消订单
        System.out.println(message);
        //因为接收到的消息前后多出了双引号，这里需要截取字符串
        String subStr = message.substring(1, message.length() - 1);

        Long orderId = Long.valueOf(subStr);
        //设置订单状态为无效订单 status - 5
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setStatus(5);
        omsOrder.setId(orderId);
        int result = omsOrderService.cancelOrder(omsOrder);
        //查询订单详情，根据详情释放锁定库存
        if (result != 0) {
            List<OmsOrderDetail> omsOrderDetails = omsOrderDetailService.queryOrderDetailsByOrderId(orderId);
            int returnStock = pmsSkuStockService.returnStock(omsOrderDetails);
            if (returnStock != 0) {
                log.info("========== Order:{} has been canceled and the stock has been returned.", orderId);
            }
        }
    }
}
