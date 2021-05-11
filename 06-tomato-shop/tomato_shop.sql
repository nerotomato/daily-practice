-- tomato_shop.ums_member definition 会员表
DROP TABLE IF EXISTS ums_member;

CREATE TABLE `ums_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(64) DEFAULT NULL COMMENT '密码',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `telephone` varchar(64) DEFAULT NULL COMMENT '手机号码',
  `status` tinyint DEFAULT NULL COMMENT '帐号启用状态:0->禁用；1->启用',
  `gender` tinyint DEFAULT NULL COMMENT '性别：0->未知；1->男；2->女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `city` varchar(64) DEFAULT NULL COMMENT '所在城市',
  `job` varchar(100) DEFAULT NULL COMMENT '职业',
  `create_time` datetime DEFAULT NULL COMMENT '注册时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_telephone` (`telephone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='会员表';

-- tomato_shop.oms_order definition 订单表
DROP TABLE IF EXISTS oms_order;

CREATE TABLE `oms_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `member_id` bigint NOT NULL COMMENT '用户id',
  `order_sn` varchar(64) DEFAULT NULL COMMENT '订单编号',
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

-- tomato_shop.pms_product definition 商品表
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='商品信息';