package com.king.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JiebaTokenizer {

    private JiebaSegmenter segmenter;

    public JiebaTokenizer() {
        this.segmenter = new JiebaSegmenter();
    }

    /**
     * 从指定文件中读取文本，并进行分词
     * @param filePath 文件路径
     * @return 返回分词结果的 Map，key 为词语，value 为出现的次数
     * @throws IOException 读取文件时可能抛出的异常
     */
    public Map<String, Integer> tokenizeFromFile(String filePath) throws IOException {
        Map<String, Integer> result = new HashMap<>();
        StringBuilder content = new StringBuilder();

        // 读取文件内容
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        // 去除所有非汉字字符
        String cleanedContent = content.toString().replaceAll("[^\\u4e00-\\u9fa5]", "");

        // 使用 Jieba 分词
        List<SegToken> tokens = segmenter.process(cleanedContent, JiebaSegmenter.SegMode.SEARCH);

        // 统计分词结果出现的次数
        for (SegToken token : tokens) {
            result.put(token.word, result.getOrDefault(token.word, 0) + 1);
        }

        return result;
    }
}
