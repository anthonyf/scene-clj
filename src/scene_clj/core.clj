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
           (com.badlogic.gdx.graphics.glutils ShapeRenderer
                                              ShapeRenderer$ShapeType)
           (com.badlogic.gdx.math Matrix4)
           (com.badlogic.gdx.graphics.g2d SpriteBatch
                                          BitmapFont)
           (com.badlogic.gdx.assets.loaders.resolvers InternalFileHandleResolver)
           (com.badlogic.gdx.assets AssetManager)
           (com.badlogic.gdx.graphics.g2d.freetype FreeTypeFontGenerator
                                                   FreeTypeFontGeneratorLoader
                                                   FreetypeFontLoader
            FreetypeFontLoader$FreeTypeFontLoaderParameter))
  (:require [clojure.stacktrace :as stack]
            [scene-clj.drawing :as d]
            [scene-clj.behavior :as b]))

(def scene
  "The scene graph"
  (atom nil))

(def ^:private context (atom nil))

(defn- make-application
  [width height]
  (let [indentity-matrix (Matrix4.)
        app (proxy [ApplicationAdapter] []

              (create []
                (let [camera (OrthographicCamera. width height)
                      asset-manager (AssetManager.)
                      resolver (InternalFileHandleResolver.)]

                  ;; set up ttf font loader support for asset manager
                  (.setLoader asset-manager FreeTypeFontGenerator (FreeTypeFontGeneratorLoader. resolver))
                  (.setLoader asset-manager BitmapFont ".ttf" (FreetypeFontLoader. resolver))

                  (reset! context
                          {:shape-renderer (ShapeRenderer.)
                           :sprite-batch (SpriteBatch.)
                           :camera camera
                           :viewport (FitViewport. width height camera)
                           :asset-manager asset-manager}))
                (.update (:camera @context))
                (.idt #^Matrix4 indentity-matrix)
                (proxy-super create))

              (render []
                (let [{:keys [shape-renderer sprite-batch camera viewport]} @context]
                  (.glClearColor Gdx/gl 0 0 0 1)
                  (.glClear Gdx/gl GL30/GL_COLOR_BUFFER_BIT)

                  (.setProjectionMatrix #^ShapeRenderer shape-renderer
                                        (.combined #^OrthographicCamera camera))
                  (.setTransformMatrix #^ShapeRenderer shape-renderer indentity-matrix)
                  (.setProjectionMatrix #^SpriteBatch sprite-batch
                                        (.combined #^OrthographicCamera camera))
                  (.setTransformMatrix #^SpriteBatch sprite-batch indentity-matrix)

                  ;; draw a box around the game screen
                  (.begin shape-renderer ShapeRenderer$ShapeType/Line)
                  (let [[sw sh] [width height]
                        sw (- sw 1)
                        sh (- sh 1)]
                    (.line shape-renderer 1 1 sw 1)
                    (.line shape-renderer sw 1 sw sh)
                    (.line shape-renderer sw sh 1 sh)
                    (.line shape-renderer 1 sh 1 1))
                  (.end shape-renderer)

                  (try
                    (reset! scene
                            (b/behave (.getDeltaTime Gdx/graphics)
                                      @scene
                                      []
                                      @scene))
                    (binding [d/*current-batch* nil]
                      (d/draw @context
                              @scene)
                      (d/finish-batch))

                    (catch Exception e
                      (stack/print-stack-trace e)
                      (flush)
                      (reset! scene nil)))

                  (proxy-super render)))

              (resize [width height]
                (let [{:keys [viewport]} @context]
                  (.update viewport width height true))
                (proxy-super resize width height))

              (dispose []
                (let [{:keys [shape-renderer sprite-batch]} @context]
                  (.dispose shape-renderer)
                  (.dispose sprite-batch))))]
    app))

(defn run-scene
  [& {:keys [screen-width screen-height resizable title]
            :or {screen-width 1024 screen-height 768 resizable true title "scene-clj"}}]
  (let [config (doto (LwjglApplicationConfiguration.)
                 (-> .title (set! title))
                 (-> .width (set! screen-width))
                 (-> .height (set! screen-height))
                 (-> .resizable (set! resizable)))]
    (LwjglApplication. (make-application screen-width screen-height) config)
    (Keyboard/enableRepeatEvents true)))


(defn -main
  [& args]
  (run-scene))
