(ns anekantavada.text.boot
  "Boot tasks for command-line text processing"
  (:require [boot.core :refer [deftask with-pre-wrap] :as core]
            [boot.task.built-in :refer [sift]]
            [clojure.java.io :as io]
            [anekantavada.text.io.word :refer [docx->paragraphs docx->words]]
            [anekantavada.text.topic :refer [make-instance-list run-lda]]
            [clojure.string :as str]
            [me.raynes.fs :refer [base-name]])
  (:import [cc.mallet.topics.tui TopicTrainer]))

(defn- lc->uc
  [path]
  (.replaceAll path "\\.docx$" ".txt"))

(defn- compile-lc!
  [in-file out-file]
  (doto out-file
    io/make-parents
    (spit (str (str/trim (str/replace (docx->words in-file) #"\n\n+" "\n\n"))))))

(deftask plain-text
  "Extract text from docx files."
  []
  (let [tmp (core/tmp-dir!)]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (core/empty-dir! tmp)
        (let [in-files (core/input-files fileset)
              lc-files (core/by-ext [".docx"] in-files)]
          (doseq [in lc-files]
            (let [in-file  (core/tmp-file in)
                  in-path  (core/tmp-path in)
                  out-path (lc->uc in-path)
                  out-file (io/file tmp out-path)]
              (compile-lc! in-file out-file)))
          (-> fileset
              (core/add-resource tmp)
              (core/commit!)
              (next-handler)))))))

(defn row-format [id text]
  (str/join "\t" [id "x" text]))

(defn mallet-row [file]
  (row-format file (str/replace (slurp file) #"\n+" " \\n ")))

(defn mallet-paragraph-rows [file]
  (let [paras (->> (io/reader file)
                   (line-seq)
                   (remove str/blank?))]
    (map-indexed (fn [i txt] (row-format (str file ":" i) txt)) paras)))

(deftask mallet-file
  "Turn multiple files into one file for mallet"
  []
  (let [tmp (core/tmp-dir!)]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (core/empty-dir! tmp)
        (let [in-files (core/input-files fileset)
              text-files (map core/tmp-file (core/by-ext [".txt"] in-files))
              out-path "input.mallet"
              out-file (io/file tmp out-path)]
          (doto out-file
            io/make-parents
            (spit (str (str/join "\n" (mapcat mallet-paragraph-rows text-files)) "\n")))
          (-> fileset
              (core/add-resource tmp)
              (core/commit!)
              (next-handler)))))))

(deftask mallet-format
  "Equivalent of Mallet's Csv2Vectors"
  []
  (let [tmp (core/tmp-dir!)]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (require 'anekantavada.text.io.mallet)
        (core/empty-dir!)
        (let [mallet-file (core/tmp-file (first (core/by-ext [".mallet"] (core/input-files fileset))))
              out-file (doto (io/file tmp "output.instancelist") io/make-parents)]
          (apply (resolve 'anekantavada.text.io.mallet/mallet-format) mallet-file [out-file])
          (-> fileset
              (core/add-resource tmp)
              (core/commit!)
              (next-handler)))))))

(deftask lda
  "Output an LDA topic model"
  [t topics COUNT int "The number of topics."
   i iterations COUNT int "The number of iterations."
   a alpha FLOAT float "The alpha parameter."
   b beta  FLOAT float "The beta parameter."]
  (let [tmp (core/tmp-dir!)]
    (fn middlware [next-handler]
      (fn handler [fileset]
        (require 'anekantavada.text.io.mallet)
        (core/empty-dir! tmp)
        (let [in-files (core/input-files fileset)
              instance-list (apply (resolve 'anekantavada.text.io.mallet/load-instance-list) (vector (core/tmp-file (first (core/by-ext [".instancelist"] in-files)))))
              out-topics (io/file tmp "lda.topics")
              out-words (io/file tmp "lda.words")
              out-state (io/file tmp "lda.state.gz")
              lda  (run-lda instance-list
                            {:num-topics topics
                             :num-iter iterations
                             :alpha (or alpha 0.01)
                             :beta (or beta 0.01)})]
          (with-open [out (java.io.PrintWriter. (io/writer out-topics))]
            (.printTopicDocuments lda out 2))
          (.printTopWords lda out-words 10 false)
          (.printState lda out-state)
          (-> fileset
              (core/add-resource tmp)
              (core/commit!)
              (next-handler)))))))
