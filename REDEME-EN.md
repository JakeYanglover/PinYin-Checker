# Pinyin Spell Checker

This project is a Pinyin-based spell checker, which corrects user input Pinyin strings based on an edit distance algorithm and a language model. The tool also suggests valid Chinese words associated with the corrected Pinyin, using a dictionary and frequency data.

## Features

- **Pinyin word suggestions**: Given an input Pinyin string, the tool retrieves the most frequent associated Chinese words from a dictionary.
- **Pinyin spell correction**: If the input Pinyin is not found in the dictionary, the tool uses edit distance (distance 1 and 2) to suggest possible corrections.
- **Language model-based ranking**: Suggestions are ranked based on a Pinyin language model (from train.txt), which is used to estimate the likelihood of each suggestion.

## How It Works

1. **Dictionary and Language Model**:

- The tool reads a dictionary from dictionary.txt, which maps Pinyin strings to Chinese words and their frequencies.
- The language model is trained using a Pinyin frequency list from train.txt.

2. **Input Handling**:

- When a user enters a Pinyin string, the tool first checks if it's a valid Pinyin in the dictionary.
- If valid, it returns the top 5 most frequent Chinese words associated with that Pinyin.
- If the input is invalid, the tool uses the edit distance algorithm to find Pinyin strings that are within a distance of 1 or 2 edits.

3. **Spell Correction**:

- For misspelled Pinyin, the tool corrects the input using both distance-1 and distance-2 sets.
- The corrected Pinyin suggestions are ranked by their frequency in the language model.

4. **Suggestions**:

- The tool suggests possible correct Pinyin strings along with their most probable corresponding Chinese words.

## Files

- **SpellChecker.java**: The main implementation of the Pinyin spell checker.
- **dictionary.txt**: The dictionary file that maps Pinyin to Chinese words and their frequencies.
- **train.txt**: The language model file that contains a list of Pinyin strings and their frequencies for training.

## Installation and Usage

1. **Clone the Repository**:

```bash
git clone https://github.com/your-repository-url.git
```

2. **Prepare the Dictionary and Training Data**:

- `dictionary.txt` should be in the following format:

```arduino
pinyin word frequency
```

Example:

```
ni 你 0.3
hao 好 0.25
```

- `train.txt` should be in the following format:

```
pinyin frequency
```

Example:

```
ni 0.3
hao 0.25
```

3. **Run the Program**:

- Compile the program:

```bash
javac com/king/service/SpellChecker.java
```

- Run the program:

```
java com.king.service.SpellChecker
```

4. **Input and Output**:

- When prompted, input a Pinyin string:

```css
Input: ni
```

- If the input is valid, the tool will return the top 5 most frequent words:

```markdown
1. 你
2. 妮
3. 呢
...
```

- If the input is invalid, the tool will suggest corrected Pinyin along with their associated words:

```scss
你是否想要输入：
1. ni(你)
2. ni(妮)
3. li(李)
...
```

5. **Exit**:

- To exit the program, type byebye.

## Training the Language Model

To train the language model, you can update the `train.txt` file with the Pinyin frequency data from your corpus. For example:

```
zai 0.15
hao 0.12
ni 0.1
```

The more data you include in `train.txt`, the more accurate the suggestions will be based on Pinyin frequencies.

## Example Usage

Here is an example of running the program:

```css
Input: ni
1. 你  2. 妮  3. 呢  4. 泥  5. 昵

Input: na
你是否想要输入：
1. ni(你)  2. nai(奶)  3. nan(男)
```

## Future Improvements

- Implement better handling for ambiguous Pinyin strings.
- Add support for tone marks to improve accuracy.
- Optimize performance for larger dictionaries and language models.
