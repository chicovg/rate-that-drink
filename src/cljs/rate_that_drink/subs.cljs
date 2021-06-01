(ns rate-that-drink.subs
  (:require [re-frame.core :as rf]
            [rate-that-drink.db :as db]))

(rf/reg-sub
 ::user
 (fn [db]
   (::db/user db)))
