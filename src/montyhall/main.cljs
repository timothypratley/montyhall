(ns ^:figwheel-hooks montyhall.main
  (:require [goog.dom :as gdom]
            [montyhall.ui :as ui]
            [reagent.core :as r]
            ["react-dom/client" :as rc]))

(defonce root
  (some-> (gdom/getElement "app")
    (rc/createRoot)))

(defn mount-app-element []
  (when root
    (.render root (r/as-element [ui/game]))))

(defonce app
  (do (mount-app-element)
      :done))

(defn ^:after-load on-reload []
  (mount-app-element))
