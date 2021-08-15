(ns rate-that-drink.controllers
  (:require [kee-frame.core :as kf]
            [rate-that-drink.events :as events]
            [rate-that-drink.routes :as routes]
            [re-frame.core :as rf]))

(defn route->name
  [route]
  (some-> route :data :name))

(defn when-route-name=
  [value route]
  (when (= (route->name route) value)
    value))

(kf/reg-controller
 ::profile
 {:params (fn [route]
            (when (-> route
                      route->name
                      routes/requires-profile?
                      nil?
                      not)
              :profile))
  :start (fn [_ _] [::events/load-profile])})

(kf/reg-controller
 ::drinks
 {:params (partial when-route-name= :drinks)
  :start (fn [_] [::events/load-drinks])})

(kf/reg-controller
 ::new-drink
 {:params (partial when-route-name= :new-drink)
  :start  (fn [_]
            (rf/dispatch [::events/set-selected-drink {}]))
  :stop   (fn []
            (rf/dispatch [::events/set-selected-drink nil]))})

(kf/reg-controller
 ::edit-drink
 {:params (fn [{:keys [data path-params]}]
            (when (= (:name data) :edit-drink)
              (-> path-params :id (js/Number.) int)))
  :start  (fn [_ id]
            (rf/dispatch [::events/load-and-set-selected-drink id]))
  :stop   (fn []
            (rf/dispatch [::events/set-selected-drink nil]))})
