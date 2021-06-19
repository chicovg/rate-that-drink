(ns rate-that-drink.controllers
  (:require [kee-frame.core :as kf]
            [rate-that-drink.events :as events]))

(kf/reg-controller
 ::profile
 {:params (fn [route] (-> route :data :name))
  :start (fn [_ route-name] [::events/load-profile route-name])})

(kf/reg-controller
 ::drinks
 {:params (fn [route]
            (when (-> route :data :name (= :drinks))
              :drinks))
  :start (fn [_] [::events/load-drinks])})
