(ns groov.core
  (:require [om.core :as om]
            [om.dom :as dom]
            [goog.events :as events]
            [ajax.core :refer [ajax-request transit-response-format]]
            [cognitect.transit :refer [reader]])
  (:import [goog.dom ViewportSizeMonitor]))

(defonce viewport-monitor (ViewportSizeMonitor.))
(defonce json-reader (reader :json))
(defonce *app-state* (atom {}))

(def grid-unit-size
  {:desktop 96
   :mobile 32})

(def mobile-cutoff 480.0)

(defn make-layout [viewport-width]
  (let [mode (if (> viewport-width mobile-cutoff) :desktop :mobile)
        grid-size (get grid-unit-size mode)
        points-per-grid (.floor js/Math (/ viewport-width grid-size))
        container-width (* grid-size points-per-grid)]
    {:mode mode
     :container-width container-width
     :points-per-grid points-per-grid
     :viewport-width viewport-width}))

(defn gadget-bounds
  "Given a gadget description and a layout map, figure out what the gadget's bounds are."
  [gadget layout]
  (let [gadget-layout (get gadget (if (= (:mode layout) :desktop) "desktopLayout" "handheldLayout"))
        points-per-grid (:points-per-grid layout)]
    {:left (* (get gadget-layout "x") points-per-grid)
     :top (* (get gadget-layout "y") points-per-grid)
     :width (* (get gadget-layout "width") points-per-grid)
     :height (* (get gadget-layout "height") points-per-grid)
     :z-index (get gadget-layout "z")}))

(defn gadget-style
  "Takes a gadget's bounds (from gadget-bounds) and returns an appropriately formatted style map"
  [bounds]
  #js {:left (str (:left bounds) "px")
       :top  (str (:top bounds) "px")
       :width  (str (:width bounds) "px")
       :height (str (:height bounds) "px")
       :z-index (:z-index bounds)})

(defn GadgetContainer
  [{:keys [gadget layout]} owner]
  (reify
    om/IRender
    (render [this]
      (let [bounds (gadget-bounds gadget layout)]
        (dom/div #js {:className "gadget-container"
                      :style (gadget-style bounds)}
                 (dom/p nil (str "I am a " (get gadget "type") " gadget.")))))))

(defn ViewportComponent
  [data owner]
  (reify
    om/IRender
    (render [this]
      (let [layout (:layout data)]
        (dom/div #js {:className "container"}
          (dom/p nil (dom/strong nil "Viewport width: " ) (:viewport-width layout))
          (dom/p nil (dom/strong nil "Layout mode: ") (str (:mode layout)))
          (dom/p nil (dom/strong nil "Points per grid unit: ") (:points-per-grid layout))
          (dom/p nil (dom/strong nil "Container width: ") (:container-width layout)))))))

(defn PagesLoadingComponent
  [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "container"}
        (dom/p nil "The pages haven't loaded yet.")))))

(defn PageComponent
  [{:keys [page layout]} owner]
  (reify
    om/IRender
    (render [this]
      (if (nil? page)
        ;; If the pages haven't loaded yet, just use an empty thing that tells the user that.
        (om/build PagesLoadingComponent nil)

        ;; Otherwise, build up the wrapper DOM and render all the gadgets.
        (dom/div #js {:className "container-fluid"}
          (dom/div #js {:className "page-wrapper"}
            (for [gadget (get page "gadgets")]
              (om/build GadgetContainer {:gadget gadget :layout layout}))))))))

(defn update-layout! []
  (let [layout (make-layout (.-width (.getSize viewport-monitor)))]
    (swap! *app-state* assoc :layout layout)))

(defn listen-for-viewport-events!
  "Start listening for viewport update events, and update *app-state* when they happen."
  []
  (events/listen viewport-monitor
                 goog.events.EventType.RESIZE
                 #(update-layout!)))

(defn load-pages!
  []
  (.log js/console "load-pages!")
  (ajax-request {:uri "/pages.json"
                 :method :get
                 :response-format (transit-response-format {:reader json-reader})
                 :handler (fn [[ok pages]]
                            (if ok
                              (swap! *app-state* assoc :pages pages)
                              (.warn js/console "Couldn't load pages. :(")))
                 :error-handler (fn [{:keys [status-text]}]
                                  (.warn js/console (str "Load pages request returned an error: " status-text)))}))

(defn LoadPagesButton
  [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "container"}
        (dom/button #js {:className "btn btn-primary"
                         :onClick (fn [e]
                                    (load-pages!))} "Load Pages")))))

(defn RootComponent
  [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (om/build ViewportComponent data)
        (om/build LoadPagesButton nil)
        (om/build PageComponent {:page (first (:pages data)) :layout (:layout data)})))))

(defn render-root []
  (om/root RootComponent *app-state*
           {:target (.getElementById js/document "app")}))

;; Main is tagged with ^:export to tell the Google Closure compiler to emit
;; the function's name as is without minifying it.
(defn ^:export main
  []
  (update-layout!)
  (listen-for-viewport-events!)
  (render-root))
