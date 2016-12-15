(ns scene-clj.drawing
  (import (com.badlogic.gdx.graphics.glutils ShapeRenderer
                                             ShapeRenderer$ShapeType)
          (com.badlogic.gdx.math Matrix4
                                 Matrix3)))

(defmulti draw (fn [context obj]
                 (cond (map? obj) (:type obj)
                       :else (class obj))))

(defmethod draw nil
  [_ _]
  )

(defmethod draw clojure.lang.Sequential
  [context col]
  (doseq [obj col]
    (draw context obj)))

(defmethod draw :group
  [context {:keys [children]}]
  (doseq [child children]
    (draw context child)))

(defn group
  [children & {:as m}]
  (merge {:type :group :children children}
         m))

(defn apply-transform
  [{:keys [shape-renderer] :as context}
   {[sx sy :as scale] :scale
    [tx ty :as translate] :translate
    rotate :rotate}
   f]
  (if (or translate scale rotate)
    (let [transform (.getTransformMatrix #^ShapeRenderer shape-renderer)
          old-transform (.cpy #^Matrix4 transform)
          m3 (.set #^Matrix3 (Matrix3.) transform)]
      (when scale
        (.scl #^Matrix3 m3 sx sy))
      (when rotate
        (.rotate #^Matrix3 m3 rotate))
      (when translate
        (.trn tx ty))
      (f (let [new-matrix (.set #^Matrix4 (Matrix4.) m3)]
           (.set #^Matrix4 (.getTransformMatrix #^ShapeRenderer shape-renderer)
                 #^Matrix3 m3)
           (f context)
           (.setTransformMatrix #^ShapeRenderer shape-renderer old-transform))))
    (f context)))

(defmethod draw :line
  [{:keys [shape-renderer] :as context}
   {:keys [x1 y1 x2 y2 color] :or {color [1 1 1 1] :as obj}
    :as obj}]
  (.begin #^ShapeRenderer shape-renderer ShapeRenderer$ShapeType/Line)
  (when color
    (let [[r g b a] color]
      (.setColor #^ShapeRenderer shape-renderer r g b a)))
  (.line #^ShapeRenderer shape-renderer x1 y1 x2 y2)
  (.end #^ShapeRenderer shape-renderer))

(defn line
  [x1 y1 x2 y2 & {:keys [color] :as m}]
  (merge {:type :line :x1 x1 :y1 y1 :x2 x2 :y2 y2 :color color}
         m))

(defmethod draw :rect
  [{:keys [shape-renderer]}
   {:keys [x y width height color filled?] :or {color [1 1 1 1]
                                                filled? false}}]
  (.begin #^ShapeRenderer shape-renderer (if filled?
                                           ShapeRenderer$ShapeType/Filled
                                           ShapeRenderer$ShapeType/Line))
  (when color
    (let [[r g b a] color]
      (.setColor #^ShapeRenderer shape-renderer r g b a)))
  (.rect #^ShapeRenderer shape-renderer x y width height)
  (.end #^ShapeRenderer shape-renderer))

(defn rect
  [x y w h & {:keys [color filled?] :as m}]
  (merge {:type :rect :x x :y y :width w :height h
          :color color :filled? filled?}
         m))

(defmethod draw :rotate
  [{:keys [shape-renderer] :as context}
   {:keys [r children] :as obj}]
  (let [transform (.getTransformMatrix #^ShapeRenderer shape-renderer)
        old-transform (.cpy #^Matrix4 transform)
        result (do
                 (.rotate #^Matrix4 transform 0 0 1 r)
                 (.setTransformMatrix #^ShapeRenderer shape-renderer transform)
                 ((get-method draw :group) context obj))]
    (.setTransformMatrix #^ShapeRenderer shape-renderer old-transform)
    result))


(defn rotate
  [r children & {:keys [] :as m}]
  (merge {:type :rotate
          :r r
          :children children}
         m))

(defmethod draw :translate
  [{:keys [shape-renderer] :as context}
   {:keys [x y children] :as obj}]
  (let [transform (.getTransformMatrix #^ShapeRenderer shape-renderer)
        old-transform (.cpy #^Matrix4 transform)
        result (do
                 (.trn #^Matrix4 transform x y 0)
                 (.setTransformMatrix #^ShapeRenderer shape-renderer transform)
                 ((get-method draw :group) context obj))]
    (.setTransformMatrix #^ShapeRenderer shape-renderer old-transform)
    result))
