(defproject trapperkeeper-live-reload "0.1.0-SNAPSHOT"
  :description "A LiveReload server for Trapperkeeper"
  :url "http://github.com/dankreek/trapperkeeper-live-reload"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  
  :plugins [[lein-npm "0.6.2"]]

  :npm {:dependencies [[livereload-js "2.2.2"]]}
)
