(defproject anekantavada.text.mining "0.1.0-SNAPSHOT"
  :description "Text mining with Mastodons"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.apache.poi/poi "3.15"]
                 [org.apache.poi/poi-scratchpad "3.15"]
                 [org.apache.poi/poi-ooxml "3.15"]
                 [org.clojure/tools.cli "0.3.5"]
                 [marcliberatore.mallet-lda "0.1.1"]
                 [cc.mallet/mallet "2.0.8"]
                 [snowball-stemmer "0.1.0"]]
  :resource-paths ["resources"]
  :main ktirio.text.mining
  :plugins [[lein-environ "1.0.0"]
            [lein-cljfmt "0.1.10"]
            [jonase/eastwood "0.2.1"]]
  :profiles {:dev
             {:dependencies [[criterium "0.4.3"]]}}
  :jvm-opts ["-Duser.timezone=UTC"
             "-XX:MaxPermSize=512m"
             "-Xmx4G"
             "-XX:+CMSClassUnloadingEnabled"
             "-XX:+UseCompressedOops"
             "-XX:+HeapDumpOnOutOfMemoryError"])
