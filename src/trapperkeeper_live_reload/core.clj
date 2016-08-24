(ns trapperkeeper-live-reload.core
  (:require [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [slingshot.slingshot :refer [throw+ try+]]
            [puppetlabs.experimental.websockets.client :refer [send!] :as ws-client])
  (:import (com.fasterxml.jackson.core JsonParseException)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Definitions

(def handshake-commands
  "Commands supported by all protocol versions and both client and server."
  {:hello {"protocols" [:required :array]
           "id" [:string]
           "name" [:string]
           "version" [:string]}})

(def protocols
  "All supported protocols, their descriptors and supported commands."
  {:conn-check-1 {:version 1
                  :url "http://livereload.com/protocols/connection-check-1"
                  :client-commands {"ping" {"token" [:required :string]}
                                    "pong" {"token" [:required :string]}}
                  :server-commands {"ping" {"token" [:required :string]}
                                    "pong" {"token" [:required :string]}}}

   :monitoring-7 {:version 7
                  :url "http://livereload.com/protocols/official-7"
                  :client-commands {"info" {"url" []
                                            "plugins" []}}
                  :server-commands {"reload" {"path" [:required :string]
                                              "liveCSS" [:boolean]
                                              "originalPath" [:string]
                                              "overrideURL" [:string]}}}})

(def supported-protocols
  "The set of livereload protocols supported by this server"
  (set (for [[_ p] protocols] (get p :url))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Private

(defn find-protocol-
  "Find a live-reload protocol description by its URL"
  [url]
  (let [protocol-key (ffirst (filter (fn [[k v]] (= (:url v) url)) protocols))]
    (if-not protocol-key
      (throw+ {:type ::unknown-protocol :url url}))
      protocol-key))

(defn handle-hello-
  [client-store ws payload]
  ;; calculate negotiated protocols, store them in the client-store and respond with the right command
  {:command "hello"
   :protocols supported-protocols})

(defn handle-ping-
  [client-store ws payload]
  ;; TODO: Validate all parts of this message with the protocol definitions above
  {:command "pong"
   :token (:token payload)})

(defn store-ws!-
  "Store a websocket connection into the provided store."
  [client-store ws]
  (swap! client-store assoc ws {}))

(defn remove-ws!-
  "Remove the given websocket connection from the ws-store"
  [client-store ws]
  (swap! client-store dissoc ws))

(defn get-client-
  "Retrieve the client map from the client store. Throws an exception if no
  client could be found."
  [client-store ws]
  (if-let [client (get @client-store ws)]
    client
    (do
      (log/error "the requested websocket client has not yet connected")
      (throw (IllegalStateException. "the requested client does not exist in the client store")))))

(defn decode-playload-
  "Attempt to decode an incoming payload from the client"
  [message]
  (try
    (json/decode message true)
    (catch JsonParseException e
      (let [error-msg (str "Could not decode the string '" message "' as JSON.")]
        (throw (IllegalStateException. error-msg))
        (log/error error-msg)))))

(defn on-connect!-
  [client-store ws]
  (store-ws!- client-store ws)
  (log/info "on-connect!" @client-store))

(defn on-error-
  [client-store ws e]
  (log/error "on-error" e))

(defn on-close!-
  [client-store ws status-code reason]
  (remove-ws!- client-store ws)
  (log/info "on-close!" @client-store))

(defn on-text!-
  [client-store ws message]
  (let [payload (decode-playload- message)
        response (json/encode
                   (case (:command payload)
                     "hello" (handle-hello- client-store ws payload)
                     "ping" (handle-ping- client-store ws payload)))]
    (log/debug "Got message" payload)
    (log/debug "Sending response" response)
    (send! ws response)))

(defn on-bytes!-
  [ws-store ws bytes offset len]
  ;; I'm thinking this won't be used
  (log/info "on-bytes!" ws bytes offset len))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public

(defn create-client-store!
  "Creates a new storage facility for each connected client and returns it."
  []
  (atom {}))

(defn get-ws-handler
  "Return the websocket handler for the live reload service"
  [client-store]
  {:on-connect (partial on-connect!- client-store)
   :on-error (partial on-error- client-store)
   :on-close (partial on-close!- client-store)
   :on-text (partial on-text!- client-store)
   :on-bytes (partial on-bytes!- client-store)})

