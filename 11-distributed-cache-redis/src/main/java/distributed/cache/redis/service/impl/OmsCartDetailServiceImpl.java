package distributed.cache.redis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import distributed.cache.redis.entity.OmsCartDetail;
import distributed.cache.redis.mapper.OmsCartDetailMapper;
import distributed.cache.redis.service.OmsCartDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@Service
public class OmsCartDetailServiceImpl extends ServiceImpl<OmsCartDetailMapper, OmsCartDetail> implements OmsCartDetailService {

    @Autowired
    OmsCartDetailMapper omsCartDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProductToCart(OmsCartDetail omsCartDetail) {
        omsCartDetailMapper.insert(omsCartDetail);
    }

    @Override
    public List<OmsCartDetail> queryCartDetailByMemberId(Long id) {
        QueryWrapper<OmsCartDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", id);
        return omsCartDetailMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCartDetails(List<OmsCartDetail> cartDetails) {
        return omsCartDetailMapper.deleteCartDetails(cartDetails);
    }
}
