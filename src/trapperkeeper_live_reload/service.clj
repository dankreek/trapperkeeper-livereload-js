(ns trapperkeeper-live-reload.service
  (:require [puppetlabs.trapperkeeper.core :as tk]
            [trapperkeeper-live-reload.core :as core]))

(tk/defservice live-reload-service
  [[:ConfigService get-in-config]
   [:WebroutingService add-websocket-handler]]
  (init [this context]
        (let [ws-store (core/create-client-store!)]
          (add-websocket-handler this (core/get-ws-handler ws-store))
          (assoc context :ws-store ws-store))))
