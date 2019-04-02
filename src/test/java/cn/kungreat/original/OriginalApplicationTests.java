package cn.kungreat.original;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OriginalApplicationTests {

    @Value("${lucene.path}")
    private String lucenePath;

    @Test
    public void contextLoads() {
        // 获得当前运行项目路径
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        System.out.println(path);
    }


}