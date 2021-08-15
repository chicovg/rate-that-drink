(ns rate-that-drink.app
  (:require [rate-that-drink.main :as main]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(main/init! false)
