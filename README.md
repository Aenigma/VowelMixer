# VowelMixer
Remaps the vowels in a text to make sentences composed of funny sounding words

Take a word, such as `jump` and transform it into something like `jimp` in a way that's consistent between inflections of the word and occurences of the word. That is, it should have the same vowel maps between inflections and use the same vowel maps each time the word is seen.


|  Source | Lemma | Converted |
|---------|-------|-----------|
|  jump   |  jump |    jimp   |
| jumping |  jump |  jimpang  |
| jumped  |  jump |   jimpod  |

Where a vowel map of the lemma, jump, is:

    a -> e
    e -> o
    i -> a
    o -> u
    u -> i

# Implementation

    seed = hash(lemma(source))
    map = createMap(seed)
    for char in source.toCharArray:
      if char is vowel:
        destination += map[char]
      else
        destination += char

# Future
[Compound word support?](http://nlp.stanford.edu/fsnlp/promo/colloc.pdf)
