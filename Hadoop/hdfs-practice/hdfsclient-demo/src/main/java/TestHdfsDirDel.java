import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 测试文件夹的删除
 *
 */
public class TestHdfsDirDel {
    @Test
    public void testDelete() throws IOException, InterruptedException, URISyntaxException {

        // 1 获取文件系统
        Configuration configuration = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://centos101:9000"), configuration, "root");

        // 2 执行删除
        fs.delete(new Path("/hdfs/file/test.txt"), true);

        // 3 关闭资源
        fs.close();
    }
}
