(ns scene-clj.example
  (:require [scene-clj.core :as scene]
            [scene-clj.drawing :refer [line rect]]
            [scene-clj.behavior :as b]))

(reset! scene/scene
        [(line 10 10 100 100
               :behavior ::rotate
               )
         (rect 20 20 200 200
               :color [1 0 0 1]
               :behavior ::rotate)])

(defmethod b/behave ::rotate
  [delta obj]
  (assoc obj :rotate 10))

(defn -main
  [& args]
  (scene/run-scene))
