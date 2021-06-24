package distributed.cache.redis;

import distributed.cache.redis.entity.*;
import distributed.cache.redis.service.*;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@Slf4j
@SpringBootApplication
@MapperScan("distributed.cache.redis.mapper")
public class DistributedCacheRedisApplication implements ApplicationRunner {

    @Autowired
    UmsMemberService umsMemberService;
    @Autowired
    PmsProductService pmsProductService;
    @Autowired
    OmsOrderService omsOrderService;
    @Autowired
    OmsCartDetailService omsCartDetailService;

    public static void main(String[] args) {
        SpringApplication.run(DistributedCacheRedisApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //1.开启两个线程，模拟用户下单

        UmsMember nero = (UmsMember) umsMemberService.queryUserByUsername("nero");
        UmsMember dante = (UmsMember) umsMemberService.queryUserByUsername("dante");

        new MyOrderThread(nero, pmsProductService, omsOrderService, omsCartDetailService).start();
        new MyOrderThread(dante, pmsProductService, omsOrderService, omsCartDetailService).start();
    }

}

@Slf4j
class MyOrderThread extends Thread {
    private UmsMember umsMember;
    private PmsProductService pmsProductService;
    private OmsCartDetailService omsCartDetailService;
    private OmsOrderService omsOrderService;


    public MyOrderThread(UmsMember umsMember, PmsProductService pmsProductService, OmsOrderService omsOrderService, OmsCartDetailService omsCartDetailService) {
        this.umsMember = umsMember;
        this.pmsProductService = pmsProductService;
        this.omsOrderService = omsOrderService;
        this.omsCartDetailService = omsCartDetailService;
    }

    @Override
    public void run() {
        //查询商品
        PmsProduct pmsProduct = pmsProductService.queryProductByName("iphone12 pro");
        //添加购物车
        OmsCartDetail omsCartDetail = new OmsCartDetail();
        omsCartDetail.setMemberId(umsMember.getId());
        omsCartDetail.setProductId(pmsProduct.getId());
        omsCartDetail.setProductName(pmsProduct.getName());
        omsCartDetail.setPrice(pmsProduct.getPrice());
        omsCartDetail.setQuantity(1); //购买数量
        omsCartDetailService.addProductToCart(omsCartDetail);

        Long orderId = 0l;
        try {
            //生成订单
            orderId = omsOrderService.generateOrder(umsMember);
            if (orderId != 0) {
                log.info("下单成功!");
                //查询该用户加入购物车的所有商品信息
                List<OmsCartDetail> cartDetails = omsCartDetailService.queryCartDetailByMemberId(umsMember.getId());
                //删除购物车
                int deleteResult = omsCartDetailService.deleteCartDetails(cartDetails);
                if (deleteResult != 0) {
                    log.info("购物车删除成功！");
                } else {
                    log.info("购物车删除失败！");
                }
            } else {
                log.error("下单失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("出现异常！下单失败！");
        }

    }
}
