package com.team.team_work.config;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Component
public class MallDatabaseInitializer implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(MallDatabaseInitializer.class);
    private static final String LOCK_NAME = "SHA2(CONCAT('01-ecommerce-init:', DATABASE()), 256)";
    private final DataSource dataSource;

    public MallDatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            if (queryInt(statement, "SELECT GET_LOCK(" + LOCK_NAME + ", 30)") != 1) {
                throw new SQLException("无法获取数据库初始化锁，请检查连接是否指定了数据库。");
            }
            try {
                int tableCount = queryInt(statement, """
                        SELECT COUNT(*) FROM information_schema.tables
                        WHERE table_schema = DATABASE()
                        """);
                if (tableCount == 0) {
                    var schema = new EncodedResource(new ClassPathResource("mall_schema.sql"), StandardCharsets.UTF_8);
                    ScriptUtils.executeSqlScript(connection, schema);
                    LOG.info("已初始化空数据库。");
                } else {
                    LOG.info("数据库已有表，跳过初始化。");
                }
            } finally {
                statement.execute("SELECT RELEASE_LOCK(" + LOCK_NAME + ")");
            }
        }
    }

    private int queryInt(Statement statement, String sql) throws SQLException {
        try (ResultSet result = statement.executeQuery(sql)) {
            if (!result.next()) {
                throw new SQLException("数据库初始化检查未返回结果。");
            }
            return result.getInt(1);
        }
    }
}
