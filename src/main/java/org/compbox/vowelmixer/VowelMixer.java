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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Kevin Raoofi
 */
public class VowelMixer {

    static final List<String> VOWELS = Arrays.asList("a", "e", "i", "o", "u");

    static long genseed(byte[] dat) {
        long result = 0;
        for (int i = 0; i < dat.length / 8; i++) {
            long tmp = 0;
            for (int j = i * 8; j < (i + 1) * 8 && j < dat.length; j++) {
                tmp = (tmp << 8) | (dat[j] & 0xFF);
            }
            result ^= tmp;
        }
        return result;
    }

    static Map<String, String> generateVowelMap(long seed) {
        final Random r = new Random(seed);
        final List<String> list = new ArrayList<>(VOWELS);
        Collections.shuffle(list, r);

        final Map<String, String> collect
                = IntStream.range(0, VOWELS.size())
                .mapToObj(i -> i)
                .collect(Collectors.toMap(i -> VOWELS.get(i), i -> list.get(i)));

        return collect;
    }

    static boolean isVowel(char c) {
        return "aeiou".indexOf(c) >= 0;
    }

    public static void main(String... args) {
        Scanner sc = new Scanner(System.in);
        VowelMixer vm = new VowelMixer();

        while (sc.hasNextLine()) {
            System.out.println(vm.mix(sc.nextLine()));
        }
    }

    private final Lemmatizer lemmatizer = new Lemmatizer();
    private final MessageDigest md;

    public VowelMixer() {
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String mix(String s) {
        Collection<String> tokenize = new HashSet<>(lemmatizer.tokenize(s));
        Map<String, String> lookup = lemmatizer.lemmatize(s);

        for (String token : tokenize) {
            final byte[] digest = md.digest(lookup.get(token)
                    .getBytes());
            final long seed = genseed(digest);
            final Map<String, String> map = generateVowelMap(seed);

            //System.out.println(map);
            final StringBuilder sb = new StringBuilder(s.length());

            for (int i = 0; i < token.length(); i++) {
                final char c = token.charAt(i);
                if (isVowel(c)) {
                    sb.append(map.get(c + ""));
                } else {
                    sb.append(c);
                }
            }

            final String tReplace = sb.toString();

            if (!token.equals(tReplace)) {
                s = s.replace(token, sb.toString());
            }
        }
        return s;
    }
}
