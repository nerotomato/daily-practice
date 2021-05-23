package com.nerotomato.shardingsphere.proxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nerotomato.shardingsphere.proxy.entity.PmsProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 商品信息 Mapper 接口
 * </p>
 *
 * @author nero
 * @since 2021-05-21
 */
@Mapper
public interface PmsProductMapper extends BaseMapper<PmsProduct> {

}
