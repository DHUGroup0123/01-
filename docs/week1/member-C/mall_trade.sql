-- 成员C：交易模块建表语句

-- 1. 购物车表
CREATE TABLE `tb_shopping_cart_item` (
  `cart_item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '购物项主键',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `goods_id` bigint(20) NOT NULL COMMENT '商品ID',
  `goods_count` int(11) NOT NULL DEFAULT '1' COMMENT '购买数量',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除(0未删,1已删)',
  PRIMARY KEY (`cart_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 2. 订单主表
CREATE TABLE `tb_order` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单主键',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '下单用户ID',
  `total_price` int(11) NOT NULL COMMENT '订单总价',
  `pay_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '支付状态(0未支付,1已支付)',
  `order_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态(0待支付~4交易成功)',
  `user_address` varchar(255) NOT NULL COMMENT '收货地址快照',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 3. 订单明细表
CREATE TABLE `tb_order_item` (
  `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细主键',
  `order_id` bigint(20) NOT NULL COMMENT '所属订单ID',
  `goods_id` bigint(20) NOT NULL COMMENT '商品ID',
  `goods_name` varchar(100) NOT NULL COMMENT '商品名称快照',
  `selling_price` int(11) NOT NULL COMMENT '成交单价快照',
  `goods_count` int(11) NOT NULL COMMENT '购买数量',
  PRIMARY KEY (`order_item_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';