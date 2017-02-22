(ns anekantavada.text.topic
  (:require [marcliberatore.mallet-lda :as lda]
            [marcliberatore.mallet-lda.misc :as lda-misc]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [marcliberatore.mallet-lda :as lda]
            [marcliberatore.mallet-lda.misc :as lda-misc])
  (:import [cc.mallet.types Alphabet FeatureSequence Instance InstanceList]
           [cc.mallet.topics ParallelTopicModel]))

(defn load-stopwords [stopwords-filepath]
  (-> stopwords-filepath
      (slurp)
      (str/split #"[\r\n]+")))

(defn make-lda-with-vector [labeled-vector topics]
  (lda/lda
   (->> labeled-vector
        (keep (fn [[label word-vector]]
                (when (seq word-vector)
                  (vector label word-vector))))
        (lda/make-instance-list))
   :num-topics topics
   :num-iter 1000))

(defn get-probability [model document]
  (let [inferencer (.getInferencer model)]
    (.getSampledDistribution inferencer document 10 1 5)))

(defn load-model [modelname]
  (-> modelname
      (io/as-file)
      (cc.mallet.topics.ParallelTopicModel/read)))

(defn get-instance [filename]
  (->> filename
       (io/as-file)
       (vector)
       (map #(vector (.getName %)
                     (-> %
                         (slurp)
                         (clojure.string/split #" "))))
       (lda/make-instance-list)
       (first)))

(defn get-instances [filename]
  (->> filename
       (io/as-file)
       (vector)
       (map #(vector (.getName %)
                     (-> %
                         (slurp)
                         (clojure.string/split #" "))))
       (lda/make-instance-list)))

(defn make-paragraph-instance [paragraphs]
  (->> paragraphs
       (map (fn [[k1 k2 d]] (vector (str k1 ":" k2)
                                    d)))))
;; TODO output to csv
(defn create-popular-terms [model]
  (let [topic-names (clojure.string/split (.toString (.getTopicAlphabet model)) #"\n" )
        top-topics (-> model (.getTopWords 20))]
    (->> (map-indexed (fn [idx topic-name]
                        (flatten (apply vector topic-name (map (fn [topic] topic) (nth top-topics idx))))) topic-names)
         (apply map list))))


(defn make-paragraph-prediction [modelname paragraphs]
  (let [model (load-model modelname)
        probabilities (map (fn [[paragraph text]]
                             (-> paragraph
                                 (lda/make-instance-list)
                                 ((partial get-probability model))
                                 (seq))))
        toptopics (->> (.getTopWords model 10)
                       (seq)
                       (mapv #(seq %)))]
    (->> (mapv #(vector %1 %2) probabilities toptopics)
         (sort-by #(first %))
         (reverse))))

(defn make-prediction [modelname testdocument]
  (let [model (load-model modelname)
        probabilities (-> testdocument
                          (get-instance)
                          ((partial get-probability model))
                          (seq))

        toptopics (->> (.getTopWords model 10)
                       (seq)
                       (mapv #(seq %)))]
    (->> (mapv #(vector %1 %2) probabilities toptopics)
         (sort-by #(first %))
         (reverse))))

(defn write-popular-terms [model filepath]
  (with-open [out-file (io/writer filepath)]
    (csv/write-csv out-file (create-popular-terms model))))

#_(defn load-file-to-predict [filepath]
  (-> (io/as-file filepath)
      (docscrub/process-file-paragraphs)))

(comment
  (def zip (->>  (mapv  #(vector %1 %2) (seq values) (mapv #(seq %) (seq toptopics))) (sort-by #(first %)))))

(defn make-instance-list [files]
  (->> (map #'lda-misc/make-document files)
       (lda/make-instance-list)))

(defn run-lda
  "Return a topic model (ParallelTopicModel) on the given
  instance-list, using the optional parameters if specified. The
  default parameters will run fairly quickly, but will not return
  high-quality topics."
  [instance-list
   {:keys [num-topics num-iter optimize-interval optimize-burn-in
           num-threads random-seed alpha beta]
    :or {alpha 0.01
         beta 0.01
         num-topics 10
         num-iter 100
         optimize-interval 10
         optimize-burn-in 20
         num-threads (.availableProcessors (Runtime/getRuntime))
         random-seed -1 }}]
  (doto (ParallelTopicModel. num-topics (* alpha num-topics) beta) 
    (.addInstances instance-list)
    (.setNumIterations num-iter)
    (.setOptimizeInterval optimize-interval)
    (.setBurninPeriod optimize-burn-in)
    (.setNumThreads num-threads)
    (.setRandomSeed random-seed)
    .estimate))
