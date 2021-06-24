package distributed.cache.redis.service.impl;

import distributed.cache.redis.entity.OmsOrderDetail;
import distributed.cache.redis.entity.PmsSkuStock;
import distributed.cache.redis.mapper.PmsSkuStockMapper;
import distributed.cache.redis.service.PmsSkuStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * sku的库存 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@Service
public class PmsSkuStockServiceImpl extends ServiceImpl<PmsSkuStockMapper, PmsSkuStock> implements PmsSkuStockService {

    @Autowired
    PmsSkuStockMapper pmsSkuStockMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProductStock(PmsSkuStock pmsSkuStock) {
        return pmsSkuStockMapper.updateProductStock(pmsSkuStock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int returnStock(List<OmsOrderDetail> omsOrderDetails) {
        /*for (OmsOrderDetail ood : omsOrderDetails) {
        }*/
        return pmsSkuStockMapper.updateProductStockList(omsOrderDetails);
    }
}
