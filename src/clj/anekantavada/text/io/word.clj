(ns anekantavada.text.io.word
  (:require [clojure.java.io :as io])
  (:import [org.apache.poi.xwpf.usermodel XWPFDocument]
           [org.apache.poi.openxml4j.opc OPCPackage]
           [org.apache.poi.xwpf.extractor XWPFWordExtractor]))

(defn docx->words [f]
  (with-open [in (io/input-stream f)]
    (let [worddoc (new XWPFDocument (OPCPackage/open in))
          extractor (new XWPFWordExtractor worddoc)]
      (.getText extractor))))

(defn docx->paragraphs [f]
  (with-open [in (io/input-stream f)]
    (let [worddoc (new org.apache.poi.xwpf.usermodel.XWPFDocument
                       (org.apache.poi.openxml4j.opc.OPCPackage/open in))
          paragraphs (.getParagraphs worddoc)]
      (mapv (fn [para] (.getText para)) paragraphs))))
