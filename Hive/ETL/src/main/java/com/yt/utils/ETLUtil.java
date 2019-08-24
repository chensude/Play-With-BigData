package com.yt.utils;

/**
 * 1,过滤脏数据
 * 2,将类别字段中 " " 替换为 ""
 * 3，替换管理视频的分隔符
 */
public class ETLUtil {

    public static String elt(String line) {
        String[] splits = line.split("\t");

        if(splits.length<9) {
            return null;
        }
        //去空格
        splits[3] = splits[3].replaceAll(" ","");
        StringBuilder etlString = new StringBuilder();
        for(int i = 0; i < splits.length; i++){
            if(i < 9){
                if(i == splits.length - 1){
                    etlString.append(splits[i]);
                }else{
                    etlString.append(splits[i] + "\t");
                }
            }else{
                if(i == splits.length - 1){
                    etlString.append(splits[i]);
                }else{
                    etlString.append(splits[i] + "&");
                }
            }
        }

        return etlString.toString();
    }

}
