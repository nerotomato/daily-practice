package com.nerotomato.shardingspherejdbc.service.impl;

import com.nerotomato.shardingspherejdbc.entity.PmsProduct;
import com.nerotomato.shardingspherejdbc.mapper.PmsProductMapper;
import com.nerotomato.shardingspherejdbc.service.PmsProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-05-21
 */
@Service
public class PmsProductServiceImpl extends ServiceImpl<PmsProductMapper, PmsProduct> implements PmsProductService {

}
