package com.nerotomato.shardingsphere.proxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nerotomato.shardingsphere.proxy.entity.PmsProduct;
import com.nerotomato.shardingsphere.proxy.mapper.PmsProductMapper;
import com.nerotomato.shardingsphere.proxy.service.PmsProductService;
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
