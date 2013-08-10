(ns stefon.asset.javascript
  (:require [stefon.asset :as asset]
            [stefon.settings :as settings])
  (:use [stefon.util :only [slurp-into string-builder]])
  (:import [com.google.javascript.jscomp JSSourceFile CompilerOptions CompilationLevel WarningLevel]
           [java.util.logging Logger Level]))

;; TODO: use pools here too to avoid constructor overhead
(defn make-compiler []
  (let [compiler (com.google.javascript.jscomp.Compiler.)
        options (CompilerOptions.)]
    (.setOptionsForCompilationLevel (CompilationLevel/SIMPLE_OPTIMIZATIONS) options)
    (do
      (.setOptionsForWarningLevel (WarningLevel/QUIET) options)
      (.setLevel (Logger/getLogger "com.google.javascript.jscomp") Level/OFF))
    [compiler options]))

(defn compress-js [filename text]
  (let [[compiler options] (make-compiler)]
    (.compile compiler
              (make-array JSSourceFile 0)
              (into-array JSSourceFile [(JSSourceFile/fromCode (str filename) (str text))])
              options)
    (let [source (.toSource compiler)]
      (if (.isEmpty source)
        text
        source))))

(defrecord Js [file]
  stefon.asset.Asset
  (read-asset [this]
    (->> this
        :file
        slurp
        str
        (assoc this :content))))

(asset/register "js" map->Js)