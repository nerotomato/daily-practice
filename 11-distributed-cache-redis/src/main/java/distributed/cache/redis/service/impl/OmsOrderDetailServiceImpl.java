package distributed.cache.redis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import distributed.cache.redis.entity.OmsOrderDetail;
import distributed.cache.redis.mapper.OmsOrderDetailMapper;
import distributed.cache.redis.service.OmsOrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@Service
public class OmsOrderDetailServiceImpl extends ServiceImpl<OmsOrderDetailMapper, OmsOrderDetail> implements OmsOrderDetailService {

    @Autowired
    OmsOrderDetailMapper omsOrderDetailMapper;

    @Override
    public List<OmsOrderDetail> queryOrderDetailsByOrderId(Long orderId) {
        QueryWrapper<OmsOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return omsOrderDetailMapper.selectList(queryWrapper);
    }
}
