package distributed.cache.redis.service;

import distributed.cache.redis.entity.OmsCartDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface OmsCartDetailService extends IService<OmsCartDetail> {

    void addProductToCart(OmsCartDetail omsCartDetail);

    List<OmsCartDetail> queryCartDetailByMemberId(Long id);

    int deleteCartDetails(List<OmsCartDetail> cartDetails);
}
