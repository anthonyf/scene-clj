(ns scene-clj.drawing
  (:import
   (com.badlogic.gdx.graphics.glutils ShapeRenderer
                                      ShapeRenderer$ShapeType)
   (com.badlogic.gdx.graphics.g2d SpriteBatch
                                  BitmapFont)
   (com.badlogic.gdx.math Matrix4 Matrix3)
   (com.badlogic.gdx.assets AssetManager)
   (com.badlogic.gdx.graphics.g2d.freetype FreetypeFontLoader$FreeTypeFontLoaderParameter)))

(defmulti draw (fn [context obj]
                 (cond (map? obj) (let [{:keys [behavior]} obj]
                                    (if (sequential? behavior)
                                      ::comp
                                      behavior))
                       :else (class obj))))

(defmethod draw ::comp
  [{:keys [shape-renderer
           sprite-batch]
    :as context} {:keys [behavior] :as obj}]
  (let [transform (.getTransformMatrix #^ShapeRenderer shape-renderer)
        old-transform (.cpy #^Matrix4 transform)]
    (doseq [b behavior]
      ((get-method draw b) context obj))
    (.setTransformMatrix #^ShapeRenderer shape-renderer old-transform)
    (.setTransformMatrix #^SpriteBatch sprite-batch old-transform)))

(defmethod draw :default
  [_ _])

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
  (merge {:behavior :group :children children}
         m))

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
  (merge {:behavior :line :x1 x1 :y1 y1 :x2 x2 :y2 y2 :color color}
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

(defmethod draw :rotate
  [{:keys [shape-renderer
           sprite-batch] :as context}
   {:keys [degrees] :as obj}]
  (let [transform (.getTransformMatrix #^ShapeRenderer shape-renderer)]
    (.rotate #^Matrix4 transform 0 0 1 degrees)
    (.setTransformMatrix #^ShapeRenderer shape-renderer transform)
    (.setTransformMatrix #^SpriteBatch sprite-batch transform)))

(defmethod draw :scale
  [{:keys [shape-renderer
           sprite-batch] :as context}
   {:keys [sx sy] :as obj}]
  (let [transform (.getTransformMatrix #^ShapeRenderer shape-renderer)]
    (.scale #^Matrix4 transform sx sy)
    (.setTransformMatrix #^ShapeRenderer shape-renderer transform)
    (.setTransformMatrix #^SpriteBatch sprite-batch transform)))

(defmethod draw :translate
  [{:keys [shape-renderer
           sprite-batch] :as context}
   {:keys [tx ty] :or {tx 0 ty 0} :as obj}]
  (let [transform (.getTransformMatrix #^ShapeRenderer shape-renderer)]
    (.trn #^Matrix4 transform tx ty 0)
    (.setTransformMatrix #^ShapeRenderer shape-renderer transform)
    (.setTransformMatrix #^SpriteBatch sprite-batch transform)))

(defmethod draw :image
  [{:keys [sprite-batch] :as context}
   {:keys [x y texture] :as obj}]
  (.begin #^SpriteBatch sprite-batch)
  (.end  #^SpriteBatch sprite-batch))

(defmethod draw :label
  [{:keys [sprite-batch asset-manager] :as context}
   {:keys [text font size] :or {text ""} :as obj}]
  (let [font-name (str size font)]
    (when-not (.isLoaded #^AssetManager asset-manager font-name)
      (let [params (FreetypeFontLoader$FreeTypeFontLoaderParameter.)]
        (set! (.fontFileName params)
              font)
        (set! (.size (.fontParameters params))
              size)
        (.load #^AssetManager asset-manager font-name BitmapFont params)
        (.finishLoading #^AssetManager asset-manager)))
    (.begin #^SpriteBatch sprite-batch)
    (.draw (.get #^AssetManager asset-manager font-name)
           sprite-batch
           text
           (float 0) (float 0))
    (.end  #^SpriteBatch sprite-batch)))
