package io.growth.platform.profile.infrastructure.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PreDestroy;
import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class HBaseClientConfig {

    @Value("${hbase.zookeeper.quorum:localhost}")
    private String zkQuorum;

    @Value("${hbase.zookeeper.port:2181}")
    private String zkPort;

    private Connection connection;

    @Bean
    public Connection hbaseConnection() throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", zkQuorum);
        config.set("hbase.zookeeper.property.clientPort", zkPort);
        this.connection = ConnectionFactory.createConnection(config);
        return this.connection;
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
