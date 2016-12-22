(ns scene-clj.examples.longcat.core
  (:require [scene-clj.core :refer [run-scene scene assets]]
            [scene-clj.drawing :as d]
            [scene-clj.behavior :as b]))

(def screen-size [1024 748])
(def title "Longcat")

(reset! assets [{:type :texture
                 :file "images/head.png"}])

(reset! scene [])

(defn -main
  [& args]
  (let [[width height] screen-size]
    (run-scene :screen-width width :screen-height height
               :title title)))
