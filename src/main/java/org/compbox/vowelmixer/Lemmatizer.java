/*
 * Copyright 2016 Kevin Raoofi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.compbox.vowelmixer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.WordTokenFactory;
import edu.stanford.nlp.util.CoreMap;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 *
 * @author Kevin Raoofi
 */
public class Lemmatizer {

    private static final Properties lemmatimezerProperties = new Properties();

    static {
        lemmatimezerProperties.put("annotators", "tokenize, ssplit, pos, lemma");
    }

    private final StanfordCoreNLP lemmaPipe;
    private final Map<String, String> lookup;

    public Lemmatizer() {
        lemmaPipe = new StanfordCoreNLP(lemmatimezerProperties);

        lookup = new WeakHashMap<>();
    }

    private Map<String, String> lemmatizeMap(String s) {
        final Annotation document = new Annotation(s);
        this.lemmaPipe.annotate(document);

        final Map<String, String> wordLemmaMap = document.get(
                CoreAnnotations.SentencesAnnotation.class)
                .stream()
                .flatMap((CoreMap sentence) -> sentence.get(
                        CoreAnnotations.TokensAnnotation.class)
                        .stream())
                .collect(Collectors.toMap(CoreLabel::word,
                        token -> token
                        .get(CoreAnnotations.LemmaAnnotation.class),
                        (m1, m2) -> m1));

        return wordLemmaMap;
    }

    public List<String> tokenize(String s) {
        final Tokenizer<Word> ptbTokenizer = new PTBTokenizer<>(
                new StringReader(s),
                new WordTokenFactory(),
                "");

        return ptbTokenizer.tokenize()
                .stream()
                .map(Word::word)
                .collect(Collectors.toList());
    }

    public Map<String, String> lemmatize(String s) {
        return tokenize(s)
                .stream()
                .distinct()
                .collect(Collectors.toMap(token -> token,
                        this::lemmatizeWord,
                        (m1, m2) -> m1));
    }

    private String lemmatizeWord(String s) {
        String lemma = this.lookup.get(s);
        if (lemma != null) {
            return lemma;
        }

        final Map<String, String> lemmas = this.lemmatizeMap(s);
        this.lookup.putAll(this.lemmatizeMap(s));

        lemma = lemmas.get(s);

        return lemma;
    }

    public static void main(String... args) throws InterruptedException {
        Lemmatizer lemmatizer = new Lemmatizer();
        lemmatizer.lemmatizeMap("foo");

        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) {
            long start = System.nanoTime();
            lemmatizer.lemmatize(
                    "I'm not sure yet. I was thinking about making pizza or pancakes. "
                    + "It's going to be pancakes.");
            long end = System.nanoTime();
        }
    }
}
