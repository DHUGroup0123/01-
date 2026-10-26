-- 01电商系统 V001 synthetic seed. Existing rows are not overwritten.

INSERT INTO schema_version (version, description)
SELECT 'V001', '01电商系统第一周基线'
WHERE NOT EXISTS (SELECT 1 FROM schema_version WHERE version = 'V001');

INSERT INTO category (category_id,parent_id,category_level,category_name)
SELECT 1,NULL,1,'数码与生活'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_id = 1);

INSERT INTO category (category_id,parent_id,category_level,category_name)
SELECT 2,1,2,'学习办公'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_id = 2);

INSERT INTO category (category_id,parent_id,category_level,category_name)
SELECT 3,2,3,'桌面好物'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_id = 3);

INSERT INTO product (product_id,category_id,product_name,summary,original_price,selling_price,stock,sell_status)
SELECT 1,3,'无线办公鼠标','轻巧便携，用于商品字段与价格展示验证',129,89,60,1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id = 1);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order)
SELECT 1,1,1,1
WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type = 1 AND product_id = 1);

INSERT INTO product (product_id,category_id,product_name,summary,original_price,selling_price,stock,sell_status)
SELECT 2,3,'学习笔记本套装','纸张与装订信息由成员 B 后续补充',49,29,100,1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id = 2);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order)
SELECT 2,2,2,2
WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type = 2 AND product_id = 2);

INSERT INTO product (product_id,category_id,product_name,summary,original_price,selling_price,stock,sell_status)
SELECT 3,3,'简约桌面台灯','本地演示商品，不接入真实销售渠道',199,159,30,1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id = 3);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order)
SELECT 3,3,3,3
WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type = 3 AND product_id = 3);
