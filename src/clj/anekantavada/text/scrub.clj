(ns anekantavada.text.scrub
  (:require [clojure.string :as str]
            [stemmer.snowball :as snowball]))

(def stemmer (snowball/stemmer :english))


(defn word-length-valid [word min max]
  (cond (>= (count word) min) (<= (count word) max)
        :else false))

(defn min-paragraph-length-valid [para minwords]
  ;;
  (> (count para) minwords))

(defn parse-paragraph [word-vector min-word-length max-word-length]
  ;; Removes words that are less than the min word length and more than the max word length.
  (->> word-vector
       (keep (fn [word]
               (when (word-length-valid word min-word-length max-word-length) (.trim word))))))


(defn remove-numbers [text-in]
  (.trim (str/replace text-in #"\d+" "")))


(defn n-grams [n words]
  (->> (partition n 1 words)
       (map (partial str/join " "))))

(defn remove-punctuation [text-in]
  (-> text-in
      (str/replace #"[\.,\?:;!\"'†+\/\(\)\\]+$" "") ;; end of the line
      (str/replace #"^[\.,\?:;!\"'†+\/\(\)\\]+" "") ;; beginning of the line
      (str/replace #"(\S)[,\?:;!\"'†+\(\)\\]+(\S)" "$1 $2") ;; with non-space chars either side
      (str/replace #"(\S)[\.,\?:;!\"'†+\/\(\)\\]+\s" "$1 ") ;; non-space before and space after
      (str/replace #"\s[\.,\?:;!\"'†+\/\(\)\\]+(\S)" " $1") ;; space before and non-space after
      (str/replace #"\s[-\.,\?:;!\"'†+\/\(\)\\]+\s" " ") ;; space either side
      (str/replace #" – " " ") ;; pesky em-dashes
      (str/replace #"[%\/\\\|#.&\"-*\[\]\{\}£=]+" " ")))

(defn remove-stopwords [words stop-words]
  (remove (set stop-words) words))

(defn load-stopwords [stopwords-filepath]
  #_(-> stopwords-filepath
      (slurp)
      (str/split #"[\r\n]+"))
  [])

(defn clean-string-input
  "Cleans strings for create-cleaned-ngrams-vector function."
  [str-input std-stopwords-file ngram-stopwords-file]
  (let [std-stopwords (load-stopwords std-stopwords-file)
        ngram-stopwords (load-stopwords ngram-stopwords-file)
        stopwords (merge std-stopwords ngram-stopwords)
        vec-words (-> str-input
                      (.trim)
                      (remove-numbers)
                      (remove-punctuation)
                      (str/split #"\s+"))]
    (parse-paragraph
     (->> (remove-stopwords vec-words stopwords)
          (map #(.trim %))) ;; remove extra whitespaces like \n attached to a word
     4 20)))

(defn remove-punctuation-in-string [s]
  (-> s
      (str/replace #"[\.,\?:;!\"'†+\/\(\)\\]+$" "") ;; end of the line
      (str/replace #"^[\.,\?:;!\"'†+\/\(\)\\]+" "") ;; beginning of the line
      (str/replace #"(\S)[,\?:;!\"'†+\(\)\\]+(\S)" "$1 $2") ;; with non-space chars either side
      (str/replace #"(\S)[\.,\?:;!\"'†+\/\(\)\\]+\s" "$1 ") ;; non-space before and space after
      (str/replace #"\s[\.,\?:;!\"'†+\/\(\)\\]+(\S)" " $1") ;; space before and non-space after
      (str/replace #"\s[-\.,\?:;!\"'†+\/\(\)\\]+\s" " ") ;; space either side
      (str/replace #" – " " ") ;; pesky em-dashes
      (str/replace #"\d+" "")))

(defn scrub-doc [filename paragraphs]
  (->> paragraphs
       (map str/lower-case)
       (map remove-punctuation-in-string)
       (map #(str/split % #" "))
       (map #(mapv stemmer %))
       (map #(str/join " " %))
       (map (fn [n strings] (vector filename n strings)) (range))))

(defn create-cleaned-ngrams-vector
  "Takes in a string and outputs a collection of
  cleaned words or n-grams."
  ([para std-stopwords-file ngram-stopwords-file]
   (clean-string-input para std-stopwords-file ngram-stopwords-file))
  ([n para std-stopwords-file ngram-stopwords-file]
   (n-grams n (clean-string-input para std-stopwords-file ngram-stopwords-file))))
