package distributed.cache.redis.mapper;

import distributed.cache.redis.entity.OmsOrderDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 订单详情表 Mapper 接口
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface OmsOrderDetailMapper extends BaseMapper<OmsOrderDetail> {

    int insertOrderDetailList(List<OmsOrderDetail> omsOrderDetails);
}
