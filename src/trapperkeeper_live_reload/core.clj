(ns trapperkeeper-live-reload.core
  (:require [clojure.tools.logging :as log]))


(defn on-connect!
  [ws]
  ;; Store the ws so that can be later notified of filesystem changes
  (log/info "on-connect!" ws))

(defn on-error
  [ws e]
  (log/info "on-error" e))

(defn on-close!
  [ws status-code reason]
  (log/info "on-close!" ws status-code reason))

(defn on-text!
  [ws message]
  ;; Attempt to JSON decode the message and go to town
  (log/info "on-text!" message))

(defn on-bytes!
  [ws bytes offset len]
  ;; I'm thinking this won't be used
  (log/info "on-bytes!" ws bytes offset len))

(defn get-ws-handler
  []
  {:on-connect on-connect!
   :on-error on-error
   :on-close on-close!
   :on-text on-text!
   :on-bytes on-bytes!})

