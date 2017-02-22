(ns anekantavada.text.stem.snowball
  (:import [cc.mallet.pipe Pipe]
           [org.tartarus.snowball.ext englishStemmer])
  (:gen-class :extends cc.mallet.pipe.Pipe))

(defn -pipe [carrier]
  (let [stemmer (new englishStemmer)]
    (doseq [token (.getData carrier)]
      (.setCurrent stemmer (.getText token))
      (.stem stemmer)
      (.setText token (.getCurrent stemmer))))
      
  carrier)
