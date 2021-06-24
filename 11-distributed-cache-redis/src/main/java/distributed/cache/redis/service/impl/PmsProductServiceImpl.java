package distributed.cache.redis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import distributed.cache.redis.entity.PmsProduct;
import distributed.cache.redis.mapper.PmsProductMapper;
import distributed.cache.redis.service.PmsProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@Service
public class PmsProductServiceImpl extends ServiceImpl<PmsProductMapper, PmsProduct> implements PmsProductService {

    @Autowired
    PmsProductMapper pmsProductMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object addProduct(PmsProduct pmsProduct) {
        return pmsProductMapper.insert(pmsProduct);
    }

    @Override
    public PmsProduct queryProductByName(String name) {
        QueryWrapper<PmsProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return pmsProductMapper.selectOne(queryWrapper);
    }
}
