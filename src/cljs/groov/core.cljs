(ns groov.core
  (:require [om.core :as om]
            [om.dom :as dom]))

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))

(defn render-root []
  (om/root widget
           {:text "Hello world!"}
           {:target (.getElementById js/document "app")}))

;; Main is tagged with ^:export to tell the Google Closure compiler to emit
;; the function's name as is without minifying it.
(defn ^:export main
  []
  (render-root))
