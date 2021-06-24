# 1.创建数据库

**分库：tomato_shop_0、tomato_shop_1**

```sql
drop database if exists tomato_shop_0;
drop database if exists tomato_shop_1;
create database IF NOT EXISTS tomato_shop_0 default charset utf8mb4;
create database IF NOT EXISTS tomato_shop_1 default charset utf8mb4;
```

# 2.创建表

**分表：**

**创建订单表**

**每个库分别创建表 oms_order_0  到 oms_order_7，共8张表**

```sql
DROP TABLE IF EXISTS oms_order_0;

CREATE TABLE `oms_order_0` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `member_id` bigint NOT NULL COMMENT '用户id',
  `source_type` tinyint NOT NULL COMMENT '订单来源：1->APP;2->网页', 
  `member_username` varchar(64) NOT NULL COMMENT '用户帐号',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '订单总金额',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1->支付宝；2->微信; 3->其他',  
  `status` tinyint DEFAULT NULL COMMENT '订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单',
  `order_type` tinyint DEFAULT NULL COMMENT '订单类型：0->正常订单；1->秒杀订单',  
  `receiver_name` varchar(100) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) NOT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(32) DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province` varchar(32) DEFAULT NULL COMMENT '省份/直辖市',
  `receiver_city` varchar(32) DEFAULT NULL COMMENT '城市',
  `receiver_region` varchar(32) DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `note` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `confirm_status` tinyint DEFAULT NULL COMMENT '确认收货状态：0->未确认；1->已确认',
  `delete_status` tinyint NOT NULL DEFAULT '0' COMMENT '删除状态：0->未删除；1->已删除',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `comment_time` datetime DEFAULT NULL COMMENT '评价时间',
  `create_time` datetime DEFAULT NULL COMMENT '提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='订单表';
```

**创建商品表**

```sql
DROP TABLE IF EXISTS pms_product;

CREATE TABLE `pms_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌id',
  `product_category_id` bigint DEFAULT NULL COMMENT '商品分类id',
  `product_attribute_category_id` bigint DEFAULT NULL COMMENT '商品属性分类id',
  `name` varchar(64) NOT NULL COMMENT '商品名称',
  `pic` varchar(255) DEFAULT NULL,
  `product_sn` varchar(64) NOT NULL COMMENT '货号',
  `delete_status` tinyint DEFAULT NULL COMMENT '删除状态：0->未删除；1->已删除',
  `publish_status` tinyint DEFAULT NULL COMMENT '上架状态：0->下架；1->上架',
  `verify_status` tinyint DEFAULT NULL COMMENT '审核状态：0->未审核；1->审核通过',
  `sort` int DEFAULT NULL COMMENT '排序',
  `sale` int DEFAULT NULL COMMENT '销量',
  `price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '市场价',
  `sub_title` varchar(255) DEFAULT NULL COMMENT '副标题',
  `description` varchar(255) COMMENT '商品描述',
  `stock` int DEFAULT NULL COMMENT '库存',
  `low_stock` int DEFAULT NULL COMMENT '库存预警值',
  `unit` varchar(16) DEFAULT NULL COMMENT '单位',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '商品重量，默认为克',
  `keywords` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NOW() COMMENT '创建时间',
  `update_time` datetime DEFAULT NOW() COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='商品信息';
```

**创建商品库存表**

```sql
DROP TABLE IF EXISTS `pms_sku_stock`;
CREATE TABLE `pms_sku_stock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `sku_code` varchar(64) NOT NULL COMMENT 'sku编码',
  `stock` int(11) DEFAULT '0' COMMENT '库存',
  `low_stock` int(11) DEFAULT NULL COMMENT '预警库存',
  `pic` varchar(255) DEFAULT NULL COMMENT '展示图片',
  `sale` int(11) DEFAULT NULL COMMENT '销量',
  `lock_stock` int(11) DEFAULT '0' COMMENT '锁定库存',
  `sp_data` varchar(500) DEFAULT NULL COMMENT '商品销售属性，json格式',
  `create_time` datetime DEFAULT NOW() COMMENT '创建时间',
  `update_time` datetime DEFAULT NOW() COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='sku的库存';
```

**创建订单详情表**

**每个库分别创建表 oms_order_detail_0  到 oms_order_detail_7，共8张表**

```sql
DROP TABLE IF EXISTS `oms_order_detail_0`;
CREATE TABLE `oms_order_detail_0` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `member_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `product_id` bigint(20) DEFAULT NULL COMMENT '商品id',
  `product_pic` varchar(500) DEFAULT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_brand` varchar(200) DEFAULT NULL,
  `product_sn` varchar(64) DEFAULT NULL,
  `product_price` decimal(10,2) DEFAULT NULL COMMENT '销售价格',
  `real_price` decimal(10,2) DEFAULT NULL COMMENT '该商品经过优惠后的价格',
  `quantity` int(11) DEFAULT NULL COMMENT '购买数量',
  `product_sku_id` bigint(20) DEFAULT NULL COMMENT '商品sku编号',
  `product_sku_code` varchar(64) DEFAULT NULL COMMENT '商品sku条码',
  `product_category_id` bigint(20) DEFAULT NULL COMMENT '商品分类id',
  `product_attr` varchar(500) DEFAULT NULL COMMENT '商品销售属性:[{"key":"颜色","value":"颜色"},{"key":"容量","value":"4G"}]',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单详情表';
```

**创建购物车表**

```sql
DROP TABLE IF EXISTS `oms_cart_detail`;
CREATE TABLE `oms_cart_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) DEFAULT NULL,
  `product_sku_id` bigint(20) DEFAULT NULL,
  `member_id` bigint(20) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL COMMENT '购买数量',
  `price` decimal(10,2) DEFAULT NULL COMMENT '添加到购物车的价格',
  `product_pic` varchar(1000) DEFAULT NULL COMMENT '商品主图',
  `product_name` varchar(500) DEFAULT NULL COMMENT '商品名称',
  `product_sub_title` varchar(500) DEFAULT NULL COMMENT '商品副标题（卖点）',
  `product_sku_code` varchar(200) DEFAULT NULL COMMENT '商品sku条码',
  `member_nickname` varchar(500) DEFAULT NULL COMMENT '会员昵称',
  `delete_status` int(1) DEFAULT '0' COMMENT '是否删除',
  `product_category_id` bigint(20) DEFAULT NULL COMMENT '商品分类',
  `product_brand` varchar(200) DEFAULT NULL,
  `product_sn` varchar(200) DEFAULT NULL,
  `product_attr` varchar(500) DEFAULT NULL COMMENT '商品销售属性:[{"key":"颜色","value":"颜色"},{"key":"容量","value":"4G"}]',
  `create_time` datetime DEFAULT NOW() COMMENT '创建时间',
  `update_time` datetime DEFAULT NOW() COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COMMENT='购物车表';
```

