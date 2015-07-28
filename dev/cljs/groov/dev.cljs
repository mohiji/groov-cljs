(ns groov.dev
  "Starts up Figwheel for live code and CSS reloading."
  (:require [figwheel.client :as fw :include-macros true]
            [groov.core :as groov]))

;; Start Figwheel. It'll listen on a websocket on port 3449 (that's the default,
;; but it doesn't hurt to be specific), and whenever anything changes it'll call
;; groov/render-root.
(fw/start {:on-jsload #(groov/render-root)
           :websocket-url "ws://localhost:3449/figwheel-ws"})

