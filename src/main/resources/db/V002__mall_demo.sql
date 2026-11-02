-- 01电商系统 第二周可选演示数据；仅demo或sample显式加载，不覆盖已有记录。

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 101,3,'静音无线鼠标','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-mouse.svg','<p>静音无线鼠标</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',69,39,30,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=101);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1001,1,101,12 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=1 AND product_id=101);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 102,3,'便携蓝牙鼠标','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-mouse.svg','<p>便携蓝牙鼠标</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',86,56,31,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=102);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1002,1,102,11 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=1 AND product_id=102);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 103,3,'轻量办公鼠标','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-mouse.svg','<p>轻量办公鼠标</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',103,73,32,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=103);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1003,1,103,10 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=1 AND product_id=103);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 104,3,'人体工学鼠标','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-mouse.svg','<p>人体工学鼠标</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',120,90,33,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=104);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1004,1,104,9 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=1 AND product_id=104);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 105,3,'方格学习笔记本','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-notebook.svg','<p>方格学习笔记本</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',137,107,34,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=105);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1005,2,105,8 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=2 AND product_id=105);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 106,3,'日程计划手账','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-notebook.svg','<p>日程计划手账</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',154,124,35,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=106);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1006,2,106,7 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=2 AND product_id=106);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 107,3,'素色便签套装','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-notebook.svg','<p>素色便签套装</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',171,141,36,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=107);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1007,2,107,6 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=2 AND product_id=107);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 108,3,'随行记事本','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-notebook.svg','<p>随行记事本</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',188,158,37,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=108);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1008,2,108,5 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=2 AND product_id=108);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 109,3,'护眼阅读台灯','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-lamp.svg','<p>护眼阅读台灯</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',205,175,38,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=109);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1009,3,109,4 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=3 AND product_id=109);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 110,3,'折叠桌面灯','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-lamp.svg','<p>折叠桌面灯</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',222,192,39,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=110);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1010,3,110,3 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=3 AND product_id=110);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 111,3,'柔光学习台灯','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-lamp.svg','<p>柔光学习台灯</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',239,209,40,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=111);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1011,3,111,2 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=3 AND product_id=111);

INSERT INTO product (product_id,category_id,product_name,summary,cover_url,detail_html,original_price,selling_price,stock,sell_status) SELECT 112,3,'简约床头灯','为学习与日常办公准备的实用好物，课程演示商品。','/images/01-lamp.svg','<p>简约床头灯</p><p>01电商系统独立演示数据，展示图片为项目自绘示意。价格与库存用于课程功能验证。</p>',256,226,41,1 WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id=112);

INSERT INTO home_product_config (config_id,config_type,product_id,sort_order) SELECT 1012,3,112,1 WHERE NOT EXISTS (SELECT 1 FROM home_product_config WHERE config_type=3 AND product_id=112);

INSERT INTO carousel (carousel_id,image_url,redirect_url,sort_order,is_enabled) SELECT 101,'/images/01-banner.svg','/search?categoryId=3',2,1 WHERE NOT EXISTS (SELECT 1 FROM carousel WHERE carousel_id=101);

INSERT INTO carousel (carousel_id,image_url,redirect_url,sort_order,is_enabled) SELECT 102,'/images/01-banner-study.svg','/search?categoryId=3',1,1 WHERE NOT EXISTS (SELECT 1 FROM carousel WHERE carousel_id=102);
