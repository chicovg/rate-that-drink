(ns rate-that-drink.events
  (:require
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [ajax.core :as http]
   [rate-that-drink.db :as db]))

;; FX

(rf/reg-fx
 :set-location!
 (fn [url]
   (set! (.. js/document -location) url)))

(rf/reg-event-fx
 ::nav-to
 (fn [_ [_ args]]
   {:set-location! (kf/path-for args)}))

;; Setters

(rf/reg-event-db
 ::set-loading?
 (fn [db [_ path]]
   (assoc-in db [::db/loading? path] true)))

(rf/reg-event-db
 ::unset-loading?
 (fn [{::db/keys [loading?] :as db} [_ path]]
   (assoc db ::db/loading? (dissoc loading? path))))

(rf/reg-event-db
 ::set-error
 (fn [db [_ type error]]
   (assoc-in db [::db/error type] error)))

(rf/reg-event-db
 ::set-drinks
 (fn [db [_ drinks]]
   (assoc db ::db/drinks drinks)))

(rf/reg-event-db
 ::set-profile
 (fn [db [_ profile]]
   (assoc db ::db/profile profile)))

;; Chains

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
   {:dispatch-n [[::set-profile response]
                 [::nav-to [:drinks]]]}))

(kf/reg-chain
 ::load-profile
 (fn [{db :db} _]
   (when-not (::db/profile db)
     {:dispatch [::set-loading? :profile]
      :http-xhrio {:method          :get
                   :on-failure      [::set-error ::load-profile]
                   :response-format (http/transit-response-format)
                   :uri             "/api/profile"}}))
 (fn [_ [profile]]
   {:dispatch-n [[::set-profile profile]
                 [::unset-loading? :profile]]}))

(kf/reg-chain
 ::create-profile
 (fn [_ [profile]]
   {:http-xhrio {:method          :post
                 :on-failure      [::set-error ::create-profile]
                 :params          profile
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             "/api/profile"}})
 (fn [_ [_ response]]
   {:dispatch-n [[::set-user response]
                 [::nav-to [:drinks]]]}))

(kf/reg-chain
 ::edit-profile
 (fn [_ [profile]]
   {:http-xhrio {:method          :put
                 :on-failure      [::set-error ::edit-profile]
                 :params          profile
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             (str "/api/profile/" (:id profile))}})
 (fn [_ [_ response]]
   {:dispatch-n [[::set-profile response]
                 [::nav-to [:drinks]]]}))

(kf/reg-chain
 ::load-drinks
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :on-failure      [::set-error ::load-drinks]
                 :response-format (http/transit-response-format)
                 :uri             "/api/drinks"}})
 (fn [_ [drinks]]
   {:dispatch [::set-drinks drinks]}))
