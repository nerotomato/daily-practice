package distributed.cache.redis.controller;


import distributed.cache.redis.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author nero
 * @since 2021-06-21
 */
@RestController
@RequestMapping("/oms-order")
public class OmsOrderController {

    @Autowired
    OmsOrderService omsOrderService;

    @RequestMapping(value = "/queryOrderById", method = RequestMethod.GET)
    public Object queryOrderByMemberIdAndOrderId(
            @RequestParam(value = "orderId") Long orderId) {
        return omsOrderService.queryOrderByMemberIdAndOrderId(orderId);
    }

}

