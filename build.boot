(task-options!
 pom {:project 'anekantavada.text.mining
      :version "0.1.0-SNAPSHOT"})

(set-env!
 :source-paths #{"src/clj" "src/java"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.apache.poi/poi "3.15"]
                 [org.apache.poi/poi-scratchpad "3.15"]
                 [org.apache.poi/poi-ooxml "3.15"]
                 [org.clojure/tools.cli "0.3.5"]
                 [marcliberatore.mallet-lda "0.1.1"]
                 [cc.mallet/mallet "2.0.8"]
                 [snowball-stemmer "0.1.0"]
                 [me.raynes/fs "1.4.6"]
                 [com.github.rholder/snowball-stemmer "1.3.0.581.1"]
                 ])

(require '[anekantavada.text.boot :refer :all])


