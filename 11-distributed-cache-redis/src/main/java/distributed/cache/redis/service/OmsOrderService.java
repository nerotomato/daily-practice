package distributed.cache.redis.service;

import distributed.cache.redis.entity.OmsOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import distributed.cache.redis.entity.UmsMember;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface OmsOrderService extends IService<OmsOrder> {

    Long generateOrder(UmsMember umsMember) throws Exception;

    Object queryOrderByMemberIdAndOrderId(long orderId);

    int cancelOrder(OmsOrder omsOrder);
}
