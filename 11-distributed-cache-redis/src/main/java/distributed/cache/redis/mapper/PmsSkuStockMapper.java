package distributed.cache.redis.mapper;

import distributed.cache.redis.entity.OmsOrderDetail;
import distributed.cache.redis.entity.PmsSkuStock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * sku的库存 Mapper 接口
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
public interface PmsSkuStockMapper extends BaseMapper<PmsSkuStock> {

    //自定义sql,减库存
    @Update("UPDATE pms_sku_stock\n" +
            "SET stock = stock + #{stock}, update_time=CURRENT_TIMESTAMP\n" +
            "WHERE stock >= #{stock} and stock > 0 and product_id = #{productId}")
    int updateProductStock(PmsSkuStock pmsSkuStock);

    int updateProductStockList(List<OmsOrderDetail> omsOrderDetails);
}
