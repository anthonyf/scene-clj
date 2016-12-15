(ns scene-clj.behavior)

(defmulti behave (fn [delta obj]
                   (cond (map? obj) (let [{:keys [behavior]} obj]
                                      (if (sequential? behavior)
                                        ::comp
                                        behavior))
                         :else (class obj))))

(defmethod behave ::comp
  [context {:keys [behavior] :as obj}]
  (reduce (fn [obj b]
            ((get-method behave b) context obj))
          obj
          behavior))

(defmethod behave :default
  [_ obj]
  obj)

(defmethod behave :group
  [delta {:keys [children] :as obj}]
  (assoc obj :children (behave delta children)))

(defmethod behave clojure.lang.Sequential
  [delta col]
  (doall (map (fn [obj]
                (behave delta obj))
              col)))
