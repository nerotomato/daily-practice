package com.nerotomato.controller;

import com.nerotomato.entity.OmsOrder;
import com.nerotomato.entity.PageRequest;
import com.nerotomato.service.OmsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by nero on 2021/5/12.
 */
@Api(tags = "OmsOrderController")
@Controller
@RequestMapping("/omsOrder")
public class OmsOrderController {

    @Autowired
    OmsOrderService omsOrderService;

    @ApiOperation(value = "订单新增", notes = "订单新增任务详细描述", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Object save(@RequestBody OmsOrder order) {
        return omsOrderService.save(order);
    }


    //批量新增订单
    @ApiOperation(value = "订单批量新增for循环方式", notes = "订单批量新增任务详细描述", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/multiSave", method = RequestMethod.POST)
    @ResponseBody
    public Object multiSave(@RequestParam(value = "orderNums") int orderNums) {
        return omsOrderService.saveMultiOrders(orderNums);
    }

    //批量新增批处理订单
    @ApiOperation(value = "订单批量新增批处理方式", notes = "订单批量新增批处理任务详细描述", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/batchSave", method = RequestMethod.POST)
    @ResponseBody
    public Object batchSave(@RequestParam(value = "orderNums") int orderNums) {
        return omsOrderService.batchSaveOrders(orderNums);
    }

    @ApiOperation(value = "删除订单", notes = "删除会员任务详细描述", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public Object delete(@RequestBody OmsOrder order) {
        int delete = omsOrderService.delete(order);

        return "delete order of: " + order.getOrderSn() + " successfully!";
    }

    @ApiOperation(value = "查询所有订单", notes = "查询所有订单任务详细描述", httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    @ResponseBody
    public Object findAll() {
        List<OmsOrder> allOrders = omsOrderService.findAll();
        return allOrders;
    }

    @ApiOperation(value = "分页查询订单", notes = "分页查询订单任务详细描述",
            httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    /*@ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "page", value = "页数", required = true, dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "size", value = "条数", required = true, dataType = "int")
    })*/
    @ApiResponses({
            @ApiResponse(code = 500, message = "系统异常")
    })
    @RequestMapping(value = "/findByPage", method = RequestMethod.POST)
    @ResponseBody
    public Object findByPage(@RequestBody PageRequest pageRequest) {
        return omsOrderService.findByPage(pageRequest);
    }

}
