(ns trapperkeeper-live-reload.service
  (:require [puppetlabs.trapperkeeper.core :as tk]
            [trapperkeeper-live-reload.core :as core]))

(tk/defservice live-reload-service
  [[:ConfigService get-in-config]
   [:WebroutingService add-websocket-handler]]
  (init [this context]
        (add-websocket-handler this (core/get-ws-handler))
        context))
