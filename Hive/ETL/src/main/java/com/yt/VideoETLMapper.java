package com.yt;

import com.yt.utils.ETLUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class VideoETLMapper extends Mapper<LongWritable, Text,Text, NullWritable> {

    private Text k = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {


        //1.获取一行数据
        String oneLine = value.toString();

        //2.清洗数据
        String res = ETLUtil.elt(oneLine);

        if(StringUtils.isBlank(res)) return;
        //写出去
        k.set(res);

        context.write(k,NullWritable.get());

    }
}
