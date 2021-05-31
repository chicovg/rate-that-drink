(ns rate-that-drink.events
  (:require
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [ajax.core :as http]
   [rate-that-drink.db :as db]))

(rf/reg-fx
 :set-location!
 (fn [url]
   (set! (.. js/document -location) url)))

(rf/reg-event-fx
 ::nav-to
 (fn [_ [_ args]]
   {:set-location! (kf/path-for args)}))

(rf/reg-event-db
 ::set-error
 (fn [db [_ type error]]
   (assoc-in db [::db/error type] error)))

(rf/reg-event-db
 ::set-user
 (fn [db [_ user]]
   (assoc db ::db/user user)))

(kf/reg-chain
 ::login
 (fn [_ [credentials]]
   {:http-xhrio {:method          :post
                 :on-failure      [::set-error ::login]
                 :params          credentials
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             "/api/login"}})
 (fn [_ [_ response]]
   {:dispatch-n [[::set-user response]
                 [::nav-to [:drinks]]]}))

(kf/reg-chain
 ::save-profile
 (fn [_ [_ profile]]
   (prn profile)))
