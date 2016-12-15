(ns scene-clj.behavior)

(defmulti behave (fn [delta obj]
                   (cond (map? obj)
                         (do (println "test")
                             (:behavior obj)) #_ (or (:behavior obj)
                                                        (:type obj))
                         :else (class obj))))

(defmethod behave :default
  [_ obj]
  obj)

(defmethod behave :group
  [delta {:keys [children] :as obj}]
  (assoc obj :children (map (fn [obj]
                              (behave delta obj))
                            children)))

(defmethod behave clojure.lang.Sequential
  [delta col]
  (doall (map (fn [obj]
                (behave delta obj))
              col)))
