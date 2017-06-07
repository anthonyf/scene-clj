(ns scene-clj.examples.longcat.core
  (:require [scene-clj.core :refer [run-scene scene assets]]
            [scene-clj.drawing :as d]
            [scene-clj.behavior :as b]))

(def screen-size [1024 748])
(def title "Longcat")

#_ (reset! assets [{:id ::head        :type :texture :file "longcat/images/head.png"}
                   {:id ::straight    :type :texture :file "longcat/images/straight.png"}
                   {:id ::turn        :type :texture :file "longcat/images/turn.png"}
                   {:id ::tail        :type :texture :file "longcat/images/tail.png"}
                   {:id ::paws        :type :texture :file "longcat/images/paws.png"}
                   {:id ::spider-up   :type :texture :file "longcat/images/spider-up.png"}
                   {:id ::spider-down :type :texture :file "longcat/images/spider-down.png"}
                   {:id ::hairball    :type :texture :file "longcat/images/hairball"}
                   {:id ::crash       :type :texture :file "longcat/images/crash"}
                   {:id ::chomp       :type :sound :file "longcat/sounds/chomp.mp3"}
                   {:id ::scream      :type :sound :file "longcat/sounds/scream.mp3"}
                   {:id ::barf        :type :sound :file "longcat/sounds/hairball.mp3"}
                   {:id ::font-140    :type :font :file "fonts/garfield.ttf" :size 140}
                   {:id ::font-80     :type :font :file "fonts/garfield.ttf" :size 80}
                   {:id ::font-40     :type :font :file "fonts/garfield.ttf" :size 40}])

(reset! scene [{:behavior ::cat
                :head {:behavior [::cat-head :image :rotate :translate]
                       :texture "longcat/images/head.png"
                       :x 10
                       :y 10}
                :body []
                :tail {:behavior [::cat-tail :image :rotate :translate]
                       :texture "longcat/images/tail.png"}}])

(defmethod b/behave ::cat
  [delta obj]
  )

(defn -main
  [& args]
  (let [[width height] screen-size]
    (run-scene :screen-width width :screen-height height
               :title title)))
