(ns anekantavada.text.io.mallet
  (:import [java.io ObjectOutputStream FileOutputStream FileInputStream InputStreamReader Serializable]
           [cc.mallet.types InstanceList]
           [cc.mallet.pipe Pipe SerialPipes
            CharSequenceLowercase CharSequence2TokenSequence
            TokenSequenceRemoveNonAlpha TokenSequenceRemoveStopwords
            TokenSequence2FeatureSequence]
           [cc.mallet.pipe.iterator CsvIterator]
           [anekantavada.text.stem StemmerPipe]))

(def default-line-regex
  (re-pattern "^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"))

(def default-token-regex
  (re-pattern "\\p{L}[\\p{L}\\p{P}]+\\p{L}"))

(defn load-instance-list [file]
  (InstanceList/load file))

(defn mallet-format [in out]
  (let [rdr (InputStreamReader. (FileInputStream. in))
        pipes [(CharSequenceLowercase.)
               (CharSequence2TokenSequence. default-token-regex)
               (TokenSequenceRemoveNonAlpha. true)
               (TokenSequenceRemoveStopwords. false true)
               (StemmerPipe.)
               (TokenSequence2FeatureSequence.)]
        instances (doto (InstanceList. (SerialPipes. (into-array Pipe pipes)))
                    (.addThruPipe (CsvIterator. rdr default-line-regex 3 2 1)))]
    (doto (ObjectOutputStream. (FileOutputStream. out))
      (.writeObject instances)
      (.close))))
