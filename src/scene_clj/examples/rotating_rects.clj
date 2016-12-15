(ns scene-clj.examples.rotating-rects
  (:require [scene-clj.core :as scene]
            [scene-clj.drawing :refer [line]]
            [scene-clj.behavior :as b]))

(def screen-size [1024 768])

(reset! scene/scene
        (concat
         ;; bunch of animated rectangles
         (let [[width height] screen-size]
           (map (fn [_]
                  {:behavior [:rotate :translate :rect ::funtimes]
                   :tx (rand-int width) :ty (rand-int height)
                   :x -50 :y -50
                   :width 100 :height 100
                   :color [(rand) (rand) (rand) 1]
                   :degrees (rand-int 360)})
                (range 1000)))

         ;; some other test stuff
         [{:behavior :line :x1 10 :y1 10 :x2 100 :y2 100
           :color [1 0 0 1]}
          (line 35 30 200 250) ;; convenience line constructor
          ]))

(defmethod b/behave ::funtimes
  [delta obj]
  (-> obj
      ;; increase rotation of rectangle
      (update :degrees
              #(mod (inc %) 360))))

(defn -main
  [& args]
  (let [[width height] screen-size]
    (scene/run-scene :screen-width width :screen-height height)))
