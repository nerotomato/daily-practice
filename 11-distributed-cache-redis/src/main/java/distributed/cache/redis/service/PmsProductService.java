package distributed.cache.redis.service;

import distributed.cache.redis.entity.PmsProduct;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface PmsProductService extends IService<PmsProduct> {

    Object addProduct(PmsProduct pmsProduct);

    PmsProduct queryProductByName(String productName);
}
