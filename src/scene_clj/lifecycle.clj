(ns scene-clj.lifecycle)

;; (defmulti pre-render )
;; (defmulti render )
;; (defmulti post-render)

(defmulti init (fn [obj]
                 (cond (map? obj) (let [{:keys [behavior]} obj]
                                    (if (sequential? behavior)
                                      ::comp
                                      behavior))
                       :else (class obj))))

(defmethod init ::comp
  [{:keys [behavior] :as obj}]
  (reduce (fn [obj b]
            ((get-method init b) obj))
          obj
          behavior))

(defmethod init :default
  [obj]
  obj)

(defmethod init :group
  [{:keys [children] :as obj}]
  (assoc obj :children (init children)))

(defmethod init clojure.lang.Sequential
  [col]
  (doall (map (fn [obj]
                (init obj))
              col)))

(defmulti destroy (fn [obj]
                 (cond (map? obj) (let [{:keys [behavior]} obj]
                                    (if (sequential? behavior)
                                      ::comp
                                      behavior))
                       :else (class obj))))

(defmethod destroy ::comp
  [{:keys [behavior] :as obj}]
  (reduce (fn [obj b]
            ((get-method destroy b) obj))
          obj
          behavior))

(defmethod destroy :default
  [obj]
  obj)

(defmethod destroy :group
  [{:keys [children] :as obj}]
  (assoc obj :children (destroy children)))

(defmethod destroy clojure.lang.Sequential
  [col]
  (doall (map (fn [obj]
                (destroy obj))
              col)))
