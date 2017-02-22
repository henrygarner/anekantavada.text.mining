(ns anekantavada.text.io.carrot
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn row->doc [id row]
  (let [[url label text] (str/split row #"\t" 3)]
    (format "<document id=\"%s\">\n<title>default</title>\n<url>%s</url><snippet>%s</snippet></document>\n" id (str/replace (str/trim url) "&" "and") (str/replace (str/replace (str/trim text) "&" "and") "<" "LT"))))

(defn too-short? [line]
  (< (count (str/split line #" ")) 10))

(defn csv->xml [csv]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?><searchresult><query>X</query>\n"
       (apply str (map-indexed row->doc (remove too-short? (line-seq (io/reader csv)))))
       "</searchresult>\n"))
