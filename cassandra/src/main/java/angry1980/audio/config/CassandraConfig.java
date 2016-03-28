package angry1980.audio.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(CassandraConfig.HOSTS_PROPERTY_NAME)
public class CassandraConfig {

    public static final String HOSTS_PROPERTY_NAME = "music.cassandra.hosts";

    @Autowired
    private Environment env;

    @Bean(destroyMethod = "close")
    public Cluster cassandraCluster(){
        return Cluster.builder()
                .addContactPoints(env.getProperty(HOSTS_PROPERTY_NAME).split(","))
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(
                        new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build()))
                .build();
    }

    @Bean
    public Session cassandraSession(){
        return cassandraCluster().connect();
    }
}
