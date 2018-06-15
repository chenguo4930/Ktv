package com.example.administrator.ktv;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author ChengGuo
 * @date 2018/6/15
 */
public class Utile {

    /**
     * 将txt转换为String
     */
    public static String readTxt(String path) throws IOException {
        //文档内容
        StringBuffer content = new StringBuffer("");
        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            String s1 = null;
            while ((s1 = br.readLine()) != null) {
                content.append(s1 + "\n");
            }
            br.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString().trim();
    }
}
