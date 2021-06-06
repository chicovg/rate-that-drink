(ns rate-that-drink.subs
  (:require [re-frame.core :as rf]
            [rate-that-drink.db :as db]))

(rf/reg-sub
 ::loading?
 (fn [db]
   (-> db ::db/loading? not-empty boolean)))

(rf/reg-sub
 ::profile
 (fn [db]
   (::db/profile db)))
