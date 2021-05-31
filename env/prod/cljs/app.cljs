(ns rate-that-drink.app
  (:require [rate-that-drink.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init! false)
