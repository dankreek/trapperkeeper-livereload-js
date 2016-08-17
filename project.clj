(def tk-version "1.3.1")

(defproject trapperkeeper-live-reload "0.1.0-SNAPSHOT"
  :description "A LiveReload service for Trapperkeeper"
  :url "http://github.com/dankreek/trapperkeeper-livereload-js"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [puppetlabs/trapperkeeper ~tk-version]
                 [puppetlabs/trapperkeeper-webserver-jetty9 "1.5.7"]

                 ;; Transitive deps
                 [clj-time "0.7.0"]]
  
  :plugins [[lein-npm "0.6.2"]]

  :npm {:dependencies [[livereload-js "2.2.2"]]}

  ;; Adds the livereload.js to the resources path so it can be added to the .jar
  ;; This is downloaded by lein-npm during the `lein deps` phase
  :resource-paths ["node_modules/livereload-js/dist"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[puppetlabs/trapperkeeper ~tk-version :classifier "test" :scope "test"]
                                  [org.clojure/tools.namespace "0.2.11"]]}}

  :repl-options {:init-ns user}

  :main puppetlabs.trapperkeeper.main
)
