package com.king.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SpellChecker {
    private static final char[] alphabets = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String DICTIONARY_PATH = "dictionary.txt";

    public static void main(String[] args) {
        // 构建语言模型
        String path = "pinyin.txt";
        Map<String, Double> languModel = buildLanguageModel(path);

        // 加载字典
        Map<String, List<Pair<String, Double>>> dictionary = loadDictionary(DICTIONARY_PATH);

        // 读取输入的拼音
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (true){
            System.out.print("Input：");
            input = scanner.nextLine();
            if (input.equals("byebye"))
                break;
            // 尝试直接查询输入拼音的词汇
            List<Pair<String, Double>> words = dictionary.get(input);

            // 如果输入拼音在字典中合法，返回频率最高的5个词汇
            if (words != null && !words.isEmpty()) {
                // 按频率降序排序
                words.sort((pair1, pair2) -> Double.compare(pair2.getValue(), pair1.getValue()));
                // 只输出最多5个词汇
                for (int i = 0; i < Math.min(5, words.size()); i++) {
                    Pair<String, Double> word = words.get(i);
                    System.out.print((i+1)+"."+word.getKey()+" ");
                }
                System.out.println();
            }
            else {
                // 输入拼音不合法，进行纠错
                Set<String> distance1Set = legalDistance1Set(languModel, input);
                Set<String> distance2Set = legalDistance2Set(languModel, input);
                /*
                P(c|w)：在给定用户输入拼音 w 的情况下，猜测的正确词汇 c 的后验概率。这是我们希望最大化的目标。
                P(w|c)：给定正确词汇 c 时，用户输入拼音 w 的概率。这表示一个正确词汇导致特定拼音输入的可能性。
                P(c)：正确词汇 c 的先验概率，表示词汇在语言模型中的普遍性。它反映了词汇在整个语言中的频率。
                P(w)：用户输入拼音 w 的总体概率。在这个上下文中，它对所有拼音是相同的，因此可以被忽略。
                 */
                // 计算所以可能的概率
                // argmax P(c|w) = argmax P(w|c) * P(c) / P(w)
                // 在这里我们忽略P(w),因为对于可能的拼音情况，它是相同的
                // 在编辑距离相同的集合中 每个拼音的P(w|c)可视为相同，同时distance1Set的P(w|c)大于distance2Set的P(w|c)
                // 调用 guessRightWord 函数进行拼音纠错，返回可能的拼音列表
                List<String> correctedPinyinList = guessRightWord(languModel, distance1Set, distance2Set);

                // 存储所有纠错拼音对应的词汇及其频率
                List<Pair<String, Double>> allCorrectedWords = new ArrayList<>();

                // 遍历纠错后的拼音列表，并将每个拼音对应的词汇及频率加入列表
                for (String correctedPinyin : correctedPinyinList) {
                    List<Pair<String, Double>> correctedWords = dictionary.get(correctedPinyin);
                    if (correctedWords != null && !correctedWords.isEmpty()) {
                        for (Pair<String, Double> pair : correctedWords) {
                            // 将词汇(拼音)作为key，频率作为value存入allCorrectedWords中
                            String wordWithPinyin = pair.getKey() + "(" + correctedPinyin + ")";
                            allCorrectedWords.add(new Pair<>(wordWithPinyin, pair.getValue()));
                        }
                    }
                }

                // 对所有纠错拼音对应的词汇按频率进行排序
                allCorrectedWords.sort((pair1, pair2) -> Double.compare(pair2.getValue(), pair1.getValue()));

                // 输出频率最高的5个词汇
                if (!allCorrectedWords.isEmpty()) {
                    System.out.println("你是否想要输入：");
                    for (int i = 0; i < Math.min(5, allCorrectedWords.size()); i++) {
                        Pair<String, Double> word = allCorrectedWords.get(i);
                        System.out.print((i+1)+"."+word.getKey()+" ");
                    }
                    System.out.println();
                } else {
                    System.out.println("未找到相关词汇。");
                }
            }
        }

    }


    /**
     * 从指定路径的文件中构建拼音-频率的语言模型
     * @param path
     * @return 拼音-频率的映射
     */
    public static Map<String, Double> buildLanguageModel(String path) {
        Map<String, Double> languageModel = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+"); // 按空格或多个空格分割
                if (parts.length == 2) {
                    String pinyin = parts[0];
                    double frequency = Double.parseDouble(parts[1]);
                    languageModel.put(pinyin, frequency);
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
        }

        return languageModel;
    }

    /**
     * 编辑距离为1的单词集合
     *
     * @param input
     * @return
     */
    public static Set<String> buildEditDistance1Set(String input) {
        Set<String> wordsInEditDistance = new HashSet<String>();
        char[] characters = input.toCharArray();

        // 删除：删除一个字母的情况，delete letter[i]
        for (int i = 0; i < input.length(); i++) {
            wordsInEditDistance.add(input.substring(0, i) + input.substring(i + 1));
        }
        // 换位: 交换letter[i] and letter[i+1]
        for (int i = 0; i < input.length() - 1; i++) {
            wordsInEditDistance.add(input.substring(0, i) + characters[i + 1]
                    + characters[i] + input.substring(i + 2));
        }
        // 替换: 将 letter[i]替换为a-z
        for (int i = 0; i < input.length(); i++) {
            for (char c : alphabets) {
                wordsInEditDistance.add(input.substring(0, i) + c + input.substring(i + 1));
            }
        }
        // 插入: 插入一个新的字母 a-z
        for (int i = 0; i < input.length() + 1; i++) {
            for (char c : alphabets) {
                wordsInEditDistance.add(input.substring(0, i) + c + input.substring(i));
            }
        }
        return wordsInEditDistance;
    }

    /**
     * 编辑距离为2的集合.通过editDistance1函数得到编辑距离为1的集合,
     * 该集合单词再通过editDistance1函数,就可以得到编辑距离为2的集合
     *
     * @param input
     * @return
     */
    public static Set<String> buildEditDistance2Set(String input) {
        Set<String> wordsInEditDistance1 = buildEditDistance1Set(input);
        Set<String> wordsInEditDistance2 = new HashSet<String>();
        for (String editDistance1 : wordsInEditDistance1) {
            wordsInEditDistance2.addAll(buildEditDistance1Set(editDistance1));
        }
        wordsInEditDistance2.addAll(wordsInEditDistance1);
        return wordsInEditDistance2;
    }

    /**
     * 合法的编辑距离为1的单词集合
     *
     * @param languModel
     * @param input
     * @return
     */
    public static Set<String> legalDistance1Set(Map<String, Double> languModel, String input) {
        // 1. 调用 buildEditDistance1Set 生成编辑距离为1的单词集合
        Set<String> wordsInEditDistance = buildEditDistance1Set(input);

        // 2. 获取语言模型中的合法单词集合
        Set<String> legalWords = languModel.keySet();

        // 3. 创建一个新的集合来存储合法的单词
        Set<String> result = new HashSet<>();

        // 4. 过滤只保留在语言模型中存在的单词
        for (String word : wordsInEditDistance) {
            if (legalWords.contains(word)) {
                result.add(word);
            }
        }

        // 5. 返回合法的单词集合
        return result;
    }

    /**
     * 合法的编辑距离为2的单词集合
     *
     * @param languModel
     * @param input
     * @return
     */
    public static Set<String> legalDistance2Set(Map<String, Double> languModel, String input) {
        // 1. 调用 buildEditDistance1Set 生成编辑距离为1的单词集合
        Set<String> wordsInEditDistance = buildEditDistance2Set(input);

        // 2. 获取语言模型中的合法单词集合
        Set<String> legalWords = languModel.keySet();

        // 3. 创建一个新的集合来存储合法的单词
        Set<String> result = new HashSet<>();

        // 4. 过滤只保留在语言模型中存在的单词
        for (String word : wordsInEditDistance) {
            if (legalWords.contains(word)) {
                result.add(word);
            }
        }
        // 5. 返回合法的单词集合
        return result;
    }

    /**
     * 从语料库中获取正确拼音
     * @param languModel
     * @param legalDistance1Set
     * @param legalDistance2Set
     * @return
     */
    public static List<String> guessRightWord(final Map<String, Double> languModel,
                                        Set<String> legalDistance1Set,
                                        Set<String> legalDistance2Set) {
        // 如果legalDistance1Set不为空，使用它，否则使用legalDistance2Set
        Set<String> wordsInEditDistance = legalDistance1Set.isEmpty() ? legalDistance2Set : legalDistance1Set;

        // 将选定的编辑距离集合转换为列表
        List<String> words = new LinkedList<>(wordsInEditDistance);

        // 对这些单词按照语言模型中的出现概率进行排序，概率越大，排名越靠前
        Collections.sort(words, new Comparator<String>() {
            @Override
            public int compare(String word1, String word2) {
                return languModel.get(word2).compareTo(languModel.get(word1));
            }
        });

        // 返回前5个最可能的单词，如果单词少于5个，则返回所有单词
        return words.size() > 5 ? words.subList(0, 5) : words;
    }

    /**
     * 从字典文件中读取拼音、中文词汇和频率，并返回拼音到词汇及其频率的映射
     *
     * @param path
     * @return 拼音到词汇及其频率的映射
     */
    public static Map<String, List<Pair<String, Double>>> loadDictionary(String path) {
        Map<String, List<Pair<String, Double>>> pinyin2Words = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length == 3) {
                    String pinyin = parts[0];
                    String word = parts[1];
                    double frequency = Double.parseDouble(parts[2]);

                    // 将词汇和频率存入映射
                    pinyin2Words
                            .computeIfAbsent(pinyin, k -> new ArrayList<>())
                            .add(new Pair<>(word, frequency));
                }
            }
        } catch (IOException e) {
            System.err.println("读取字典文件时发生错误: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("解析频率时发生错误: " + e.getMessage());
        }

        return pinyin2Words;
    }


    // 定义一个简单的 Pair 类用于存储词汇和频率
    static class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

}
