(ns rate-that-drink.app
  (:require
    [rate-that-drink.main :as main]
    [cljs.spec.alpha :as s]
    [expound.alpha :as expound]
    [devtools.core :as devtools]))

(extend-protocol IPrintWithWriter
  js/Symbol
  (-pr-writer [sym writer _]
    (-write writer (str "\"" (.toString sym) "\""))))

(set! s/*explain-out* expound/printer)

(enable-console-print!)

(devtools/install!)

(main/init! true)
