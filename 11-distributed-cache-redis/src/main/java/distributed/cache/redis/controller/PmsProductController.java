package distributed.cache.redis.controller;


import distributed.cache.redis.entity.PmsProduct;
import distributed.cache.redis.service.PmsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 商品信息 前端控制器
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@RestController
@RequestMapping("/pms-product")
public class PmsProductController {

    @Autowired
    PmsProductService pmsProductService;

    @RequestMapping(value = "/addProduct", method = RequestMethod.POST)
    public Object save(@RequestBody PmsProduct pmsProduct) {
        return pmsProductService.addProduct(pmsProduct);
    }
}

