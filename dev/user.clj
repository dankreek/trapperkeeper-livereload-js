(ns user
  (:require [puppetlabs.trapperkeeper.bootstrap :as tk-bootstrap]
            [puppetlabs.trapperkeeper.config :as tk-config]
            [puppetlabs.trapperkeeper.app :as tk-app]
            [puppetlabs.trapperkeeper.core :as tk]
            [clojure.tools.namespace.repl :as repl] ))

(def system nil)

(defn init []
  (alter-var-root #'system
                  (fn [_] (let [services (tk-bootstrap/parse-bootstrap-config!
                                           "./dev-resources/bootstrap.cfg")
                                config (tk-config/load-config
                                         "./dev-resources/conf.d")]
                            (tk/build-app services config))))
  (alter-var-root #'system tk-app/init)
  (tk-app/check-for-errors! system))

(defn start []
  (alter-var-root #'system
                  (fn [s] (if s (tk-app/start s))))
  (tk-app/check-for-errors! system))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (tk-app/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (repl/refresh :after 'user/go))
