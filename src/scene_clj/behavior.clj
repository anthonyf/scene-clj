(ns scene-clj.behavior)

(defmulti behave (fn [delta scene keys obj]
                   (cond (map? obj) (let [{:keys [behavior]} obj]
                                      (if (sequential? behavior)
                                        ::comp
                                        behavior))
                         :else (class obj))))

(defmethod behave :default
  [_ scene _ _]
  scene)

(defmethod behave ::comp
  [delta scene keys {:keys [behavior] :as obj}]
  (reduce (fn [scene b]
            ((get-method behave b) delta scene keys obj))
          scene
          behavior))

(defmethod behave :group
  [delta scene keys {:keys [children] :as obj}]
  ;; call sequential method
  (behave delta scene (conj keys :children) children))

(defmethod behave clojure.lang.Sequential
  [delta scene keys col]
  (reduce (fn [scene [i obj]]
            (behave delta scene (conj keys i) obj))
          scene
          (map-indexed vector col)))

(defmethod behave :doodad
  [delta scene keys obj]
  (update-in scene (conj keys :foo) #(inc %)))

#_ (let [scene [{:behavior :group
                 :children [{:behavior [:foo :doodad]
                             :foo 10}]}
                {:behavior :group
                 :children [{:behavior :doodad
                             :foo 99}]}]]
     (behave 0 scene [] scene))
