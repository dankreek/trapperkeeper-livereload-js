(ns trapperkeeper-live-reload.core
  (:require [clojure.tools.logging :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Private

(defn store-ws!-
  "Store a websocket connection into the provided store."
  [ws-store ws]
  (swap! ws-store conj ws))

(defn remove-ws!-
  "Remove the given websocket connection from the ws-store"
  [ws-store ws]
  (swap! ws-store (fn [cur-store] (remove #(= ws %) cur-store))))

(defn on-connect!-
  [ws-store ws]
  ;; Store the ws so that can be later notified of filesystem changes
  (store-ws!- ws-store ws)
  (log/info "on-connect!" @ws-store))

(defn on-error!
  [ws-store ws e]
  (log/info "on-error" e))

(defn on-close!-
  [ws-store ws status-code reason]
  (remove-ws!- ws-store ws)
  (log/info "on-close!" @ws-store))

(defn on-text!-
  [ws-store ws message]
  ;; Attempt to JSON decode the message and go to town
  (log/info "on-text!" message))

(defn on-bytes!-
  [ws-store ws bytes offset len]
  ;; I'm thinking this won't be used
  (log/info "on-bytes!" ws bytes offset len))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public

(defn create-ws-store!
  "Creates a new storage facility for websocket connections and returns it."
  []
  (atom []))

(defn get-ws-handler
  "Return the websocket handler for the live reload service"
  [ws-store]
  {:on-connect (partial on-connect!- ws-store)
   :on-error (partial on-error! ws-store)
   :on-close (partial on-close!- ws-store)
   :on-text (partial on-text!- ws-store)
   :on-bytes (partial on-bytes!- ws-store)})

