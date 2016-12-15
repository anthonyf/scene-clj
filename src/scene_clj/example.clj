(ns scene-clj.example
  (:require [scene-clj.core :as scene]
            [scene-clj.drawing :refer [line rect]]
            [scene-clj.behavior :as b]))

(def screen-size [1024 768])

(reset! scene/scene
        (concat
         ;; bunch of animated rectangles
         (let [[width height] screen-size]
           (map (fn [_]
                  {:behavior [:translate ::funtimes] :x (rand-int width) :y (rand-int height)
                   :children [{:behavior :rotate :r (rand-int 360)
                               :children [(rect -50 -50 100 100
                                                :color [(rand) (rand) (rand) 1])]}]})
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
      (update-in [:children 0 :r]
                 #(mod (inc %) 360))))


(defn -main
  [& args]
  (let [[width height] screen-size]
    (scene/run-scene :screen-width width :screen-height height)))
