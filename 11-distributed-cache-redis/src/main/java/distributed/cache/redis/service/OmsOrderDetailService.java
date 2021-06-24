package distributed.cache.redis.service;

import distributed.cache.redis.entity.OmsOrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface OmsOrderDetailService extends IService<OmsOrderDetail> {

    List<OmsOrderDetail> queryOrderDetailsByOrderId(Long orderId);
}
