(ns scene-clj.examples.rotating-rects
  (:require [scene-clj.core :as scene]
            [scene-clj.drawing :refer [line]]
            [scene-clj.behavior :as b]))

(def screen-size [1024 768])

(def number-of-rects 1000)

(reset! scene/scene
        (let [[width height] screen-size]
          (vec (concat
                ;; bunch of animated rectangles
                (map (fn [_]
                       {:behavior [:rotate :translate :rect ::funtimes]
                        :tx (rand-int width) :ty (rand-int height)
                        :x -50 :y -50
                        :width 100 :height 100
                        :color [(rand) (rand) (rand) 1]
                        :degrees (rand-int 360)})
                     (range number-of-rects))

                ;; some other test stuff
                [{:behavior :line :x1 10 :y1 10 :x2 100 :y2 100
                  :color [1 0 0 1]}
                 (line 35 30 200 250) ;; convenience line constructor
                 {:behavior [::fps :translate :label]
                  :text "fps"
                  :font "fonts/SourceCodePro-Regular.ttf"
                  :size 30
                  :tx 10
                  :ty (- height 30)}]))))

((fnil + 0) nil 10)

(defmethod b/behave ::fps
  [delta scene keys {:keys [frame-count] :or {frame-count 0} :as obj}]
  (let [max-frames 30]
    (if (> frame-count max-frames)
      (-> scene
          ((fn [scene]
             (let [{:keys [frame-count frame-time]} (get-in scene keys)]
               (assoc-in scene
                         (conj keys :text)
                         (str "fps: " (Math/round (/ 1.0 (/ frame-time frame-count))))))))
          (assoc-in (conj keys :frame-count) 1)
          (assoc-in (conj keys :frame-time) delta))
      (-> scene
          (update-in (conj keys :frame-count) #((fnil inc 0) %))
          (update-in (conj keys :frame-time) #((fnil + 0) % delta))))))

(defmethod b/behave ::funtimes
  [delta scene keys obj]
  ;; increase rotation of rectangle
  (update-in scene (conj keys :degrees)
             #(mod (inc %) 360)))

(defn -main
  [& args]
  (let [[width height] screen-size]
    (scene/run-scene :screen-width width :screen-height height)))
