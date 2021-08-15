(ns rate-that-drink.events
  (:require
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [ajax.core :as http]
   [rate-that-drink.common :as common]
   [rate-that-drink.db :as db]
   [rate-that-drink.errors :as errors]))

;; FX

(rf/reg-fx
 :set-location!
 (fn [url]
   (set! (.. js/document -location) url)))

(rf/reg-event-fx
 ::nav-to
 (fn [_ [_ args]]
   {:navigate-to args}))

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

(rf/reg-event-db
 ::set-drinks-filter
 (fn [{::db/keys [drinks
                  drinks-page
                  drinks-page-size]
       :as db} [_ drinks-filter]]
   (let [filtered-page-count (->> drinks
                                  (common/filter-drinks drinks-filter)
                                  (common/drinks-page-count drinks-page-size))
         corrected-drinks-page (cond
                                 (and (zero? drinks-page) (pos? filtered-page-count)) 1
                                 (> drinks-page filtered-page-count) filtered-page-count
                                 :else drinks-page)]
     (assoc db ::db/drinks-filter drinks-filter
               ::db/drinks-page   corrected-drinks-page))))

(rf/reg-event-db
 ::set-drinks-page
 (fn [db [_ p]]
   (assoc db ::db/drinks-page p)))

(rf/reg-event-db
 ::set-selected-drink
 (fn [db [_ drink]]
   (assoc db ::db/selected-drink drink)))

(rf/reg-event-db
 ::set-drinks-sort
 (fn [db [_ sort-field]]
   (let [flip-sort-direction       (fn [dir]
                                     (condp = dir
                                       :ascending  :descending
                                       :descending :ascending
                                       nil         :ascending))
         {:keys [field direction]} (::db/drinks-sort db)]
     (assoc db ::db/drinks-sort {:field     sort-field
                                 :direction (flip-sort-direction
                                             (when (= field sort-field)
                                               direction))}))))

;; Events

(rf/reg-event-fx
 ::handle-error
 (fn [_ [_ op {:keys [status]}]]
   (cond
     (= 401 status)
     {:dispatch-n  [[::set-error :login ::errors/session-expired]
                    [::unset-loading? op]]
      :navigate-to [:login]}

     :else
     {:dispatch [::set-error :unknown ::errors/unknown]})))

;; Chains

(kf/reg-chain
 ::login
 (fn [_ [credentials]]
   {:http-xhrio {:method          :post
                 :on-failure      [::handle-error ::login]
                 :params          credentials
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             "/api/login"}})
 (fn [_ [_ response]]
   {:dispatch-n [[::set-profile response]
                 [::nav-to [:drinks]]]}))

(kf/reg-chain
 ::logout
 (fn [_ _]
   {:http-xhrio {:method          :put
                 :on-failure      [::handle-error ::logout]
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             "/api/logout"}})
 (fn [_ _]
   {:dispatch-n [[::set-profile nil]
                 [::nav-to [:home]]]}))

(kf/reg-chain
 ::load-profile
 (fn [_ _]
   {:dispatch [::set-loading? ::load-profile]
    :http-xhrio {:method          :get
                 :on-failure      [::handle-error ::load-profile]
                 :response-format (http/transit-response-format)
                 :uri             "/api/profile"}})
 (fn [_ [profile]]
     {:dispatch-n [[::set-profile profile]
                   [::unset-loading? ::load-profile]]}))

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
                 :on-failure      [::handle-error ::load-drinks]
                 :response-format (http/transit-response-format)
                 :uri             "/api/drinks"}})
 (fn [_ [drinks]]
   {:dispatch [::set-drinks drinks]}))

(kf/reg-chain
 ::load-and-set-selected-drink
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :on-failure      [::handle-error ::load-drinks]
                 :response-format (http/transit-response-format)
                 :uri             "/api/drinks"}})
 (fn [_ [id drinks]]
   (let [drink (->> drinks
                    (filter #(= id (:id %)))
                    first)]
     {:dispatch [::set-selected-drink drink]})))

(kf/reg-chain
 ::create-drink
 (fn [_ [drink]]
   {:http-xhrio {:method          :post
                 :on-failure      [::set-error ::create-drink]
                 :params          drink
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             "/api/drinks"}})
 (fn [_ _]
   {:navigate-to [:drinks]}))

(kf/reg-chain
 ::edit-drink
 (fn [_ [drink]]
   {:http-xhrio {:method          :put
                 :on-failure      [::set-error ::edit-drink]
                 :params          drink
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             (str "/api/drinks/" (:id drink))}})
 (fn [_ _]
   {:navigate-to [:drinks]}))
