package distributed.cache.redis.mapper;

import distributed.cache.redis.entity.OmsOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {
    int insertOrder(OmsOrder omsOrder);

    @Update("update oms_order set status=#{status} where id=#{id}")
    int cancelOrder(OmsOrder omsOrder);
}
