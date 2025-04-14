package configs;

import lombok.Cleanup;
import lombok.Getter;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

public class MyBatisConfig {

    @Getter
    private static final SqlSessionFactory sessionFactory = buildSessionFactory();

    private static SqlSessionFactory buildSessionFactory() {
        String resource = "mybatis-config.xml";

        try {
            @Cleanup InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            return sqlSessionFactory;

        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
