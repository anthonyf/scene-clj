(ns scene-clj.core
  (:gen-class)
  (:import (com.badlogic.gdx ApplicationAdapter Gdx)
           (com.badlogic.gdx.backends.lwjgl LwjglApplication
                                            LwjglApplicationConfiguration)
           (com.badlogic.gdx.graphics Color
                                      GL30
                                      Texture
                                      OrthographicCamera)
           (org.lwjgl.input Keyboard)
           (com.badlogic.gdx.scenes.scene2d Stage)
           (com.badlogic.gdx.utils.viewport FitViewport)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType)
           (com.badlogic.gdx.math Matrix4))
  (:require [clojure.stacktrace :as stack]
            [scene-clj.drawing :as d]
            [scene-clj.behavior :as b]))

(defn- make-application
  [scene width height]
  (let [viewport (atom nil)
        camera (atom nil)
        shape-renderer (atom nil)
        transform (atom (Matrix4.))
        app (proxy [ApplicationAdapter] []
              (create []
                (reset! camera (OrthographicCamera. width height))
                (reset! viewport (FitViewport. width height @camera))
                (reset! shape-renderer (ShapeRenderer.))
                (.update @camera)
                (proxy-super create))

              (render []
                (.glClearColor Gdx/gl 0 0 0 1)
                (.glClear Gdx/gl GL30/GL_COLOR_BUFFER_BIT)

                (.idt #^Matrix4 @transform)

                (.setProjectionMatrix #^ShapeRenderer @shape-renderer
                                      (.combined #^OrthographicCamera @camera))
                (.setTransformMatrix #^ShapeRenderer @shape-renderer @transform)

                ;; draw a box around the game screen
                (.begin @shape-renderer ShapeRenderer$ShapeType/Line)
                (let [[sw sh] [width height]
                      sw (- sw 1)
                      sh (- sh 1)]
                  (.line @shape-renderer 1 1 sw 1)
                  (.line @shape-renderer sw 1 sw sh)
                  (.line @shape-renderer sw sh 1 sh)
                  (.line @shape-renderer 1 sh 1 1))
                (.end @shape-renderer)

                (try
                  (b/behave (.getDeltaTime Gdx/graphics)
                            @scene)
                  (d/draw {:shape-renderer @shape-renderer}
                          @scene)

                  (catch Exception e
                    (stack/print-stack-trace e)
                    (flush)
                    (reset! scene nil)))

                (proxy-super render))

              (resize [width height]
                (.update @viewport width height true)
                (proxy-super resize width height))

              (dispose []
                (.dispose @shape-renderer)))]
    app))

(def scene (atom nil))

(defn run-scene
  [& {:keys [screen-width screen-height resizable title]
            :or {screen-width 1024 screen-height 768 resizable true title "scene-clj"}}]
  (let [config (doto (LwjglApplicationConfiguration.)
                 (-> .title (set! title))
                 (-> .width (set! screen-width))
                 (-> .height (set! screen-height))
                 (-> .resizable (set! resizable)))]
    (LwjglApplication. (make-application scene screen-width screen-height) config)
    (Keyboard/enableRepeatEvents true)))


(defn -main
  [& args]
  (run-scene))
