package com.yt.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class WordCountMapper extends Mapper<LongWritable,Text, Text, IntWritable> {
    Text k = new Text();
    IntWritable v= new IntWritable(1);

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String oneLine = value.toString();

        String[] words = oneLine.split(" ");
        for (String word:words) {
            k.set(word);
           context.write(k,v);
        }
    }
}
