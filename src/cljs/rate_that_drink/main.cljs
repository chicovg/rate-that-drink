(ns rate-that-drink.main
  (:require
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [rate-that-drink.controllers]
   [rate-that-drink.db :as db]
   [rate-that-drink.routes :as routes]
   [rate-that-drink.views :as views]))

(defn ^:dev/after-load mount-components
  ([] (mount-components true))
  ([debug?]
   (rf/clear-subscription-cache!)
   (kf/start! {:debug?         (boolean debug?)
               :hash-routing?  true
               :initial-db     db/initial-db
               :routes         routes/routes
               :root-component [views/root-component]})))

(defn init!
  [debug?]
  (mount-components debug?))
