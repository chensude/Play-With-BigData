import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 测试Hdfs重命名文件夹
 */
public class TestHdfsRenameDir {

    @Test
    public void testRename() throws IOException, InterruptedException, URISyntaxException {

        // 1 获取文件系统
        Configuration configuration = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://centos101:9000"), configuration, "root");

        // 2 修改文件名称
        fs.rename(new Path("/hdfs/file/test.txt"), new Path("/hdfs/file/test1.txt"));

        // 3 关闭资源
        fs.close();
    }
}
