package distributed.cache.redis.service;

import distributed.cache.redis.entity.OmsOrderDetail;
import distributed.cache.redis.entity.PmsSkuStock;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * sku的库存 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface PmsSkuStockService extends IService<PmsSkuStock> {

    int updateProductStock(PmsSkuStock pmsSkuStock);

    int returnStock(List<OmsOrderDetail> omsOrderDetails);
}
