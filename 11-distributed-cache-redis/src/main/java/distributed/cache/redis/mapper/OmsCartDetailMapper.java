package distributed.cache.redis.mapper;

import distributed.cache.redis.entity.OmsCartDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 购物车表 Mapper 接口
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface OmsCartDetailMapper extends BaseMapper<OmsCartDetail> {

    int deleteCartDetails(List<OmsCartDetail> cartDetails);
}
