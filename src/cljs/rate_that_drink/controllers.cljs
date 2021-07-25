(ns rate-that-drink.controllers
  (:require [kee-frame.core :as kf]
            [rate-that-drink.events :as events]
            [rate-that-drink.routes :as routes]
            [re-frame.core :as rf]))

(kf/reg-controller
 ::profile
 {:params (fn [route]
            (-> route
                :data
                :name
                routes/requires-profile?
                nil?
                not))
  :start (fn [_ _] [::events/load-profile])})

(kf/reg-controller
 ::drinks
 {:params (fn [route]
            (-> route
                :data
                :name
                (= :drinks)))
  :start (fn [_] [::events/load-drinks])})

(kf/reg-controller
 ::edit-drink
 {:params (fn [{:keys [data path-params]}]
            (when (= (:name data) :edit-drink)
              (-> path-params :id (js/Number.) int)))
  :start (fn [_ id]
           (rf/dispatch [::events/set-selected-drink-id id]))})
