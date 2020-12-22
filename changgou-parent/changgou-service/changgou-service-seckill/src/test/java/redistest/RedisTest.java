package redistest;

import com.changgou.seckill.pojo.SeckillGoods;
import javafx.application.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test(){
        //[1131814839669362688, 1131814839702917120]
        Set keys = redisTemplate.boundHashOps("SeckillGoods_" + "2020122018").keys();
        System.out.println(keys);
//        for (Object key : keys) {
//            SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + "2020122018").get(key);
//            System.out.println(goods.getNum());
//        }
//        String time="2020122018";
//        Long id=1131814839702917120L;
//        SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
//        System.out.println(goods);
    }

}
