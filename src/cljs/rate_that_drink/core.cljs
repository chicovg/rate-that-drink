(ns rate-that-drink.core
  (:require
   [cljsjs.semantic-ui-react :refer [Button
                                     Container
                                     Divider
                                     Form
                                     Form.Input
                                     Header
                                     Menu
                                     Menu.Item
                                     Segment]]
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [ajax.core :as http]))

;; DB

(def initial-db {::drinks []})

;; Subs

;; Events

(rf/reg-fx
 :set-location!
 (fn [url]
   (set! (.. js/document -location) url)))

(rf/reg-event-fx
 ::nav-to
 (fn [_ [_ args]]
   {:set-location! (kf/path-for args)}))

(rf/reg-event-db
 ::set-login-error
 (fn [db error]
   (assoc db :login-error error)))

(kf/reg-chain
 ::login
 (fn [_ [credentials]]
   {:http-xhrio {:method          :post
                 :on-failure      [::set-login-error]
                 :params          credentials
                 :format          (http/transit-request-format)
                 :response-format (http/transit-response-format)
                 :uri             "/api/login"}})
 (fn [{:keys [db]} [_ response]]
   (prn "login response:" response)))

(kf/reg-chain
 ::save-profile
 (fn [_ [_ profile]]
   (prn profile)))

;; Routes

(def route-info
  [[:home     "/"         "Home"]
   [:drinks   "/drinks"   "Drinks"]
   [:login    "/login"    "Login"]
   [:logout   "/logout"   "Logout"]
   [:profile  "/profile"  "Profile"]
   [:register "/register" "Register"]])

(def routes (mapv
             (fn [[key link _]] [link key])
             route-info))

(def route-text-map (reduce
                     (fn [map [key _ label]] (assoc map key label))
                     {}
                     route-info))

;; Controllers

;; Views

(defn route->text
  [route]
  (get route-text-map route))

(defn navbar
  []
  [:> Menu {:stackable true}
   (for [[_ route] routes]
     ^{:key route} [:> Menu.Item
                    {:name (name route)
                     :on-click #(rf/dispatch [::nav-to [route]])}
                    (route->text route)])])

(defn home-page
  []
  [:> Segment
   [:> Header {:as "h2"} "Home"]])

(defn login-page
  []
  (let [email (r/atom "")
        password (r/atom "")]
    (fn []
      [:> Segment
       [:> Header {:as :h2} "Enter your email and password"]
       [:> Form {:on-submit #(rf/dispatch [::login {:email @email
                                                    :password @password}])}
        [:> Form.Input {:label       "Email"
                        :name        :email
                        :placeholder "Email"
                        :on-change   #(reset! email (-> % .-target .-value))
                        :required    true
                        :type        "email"
                        :value       @email}]
        [:> Form.Input {:label       "Password"
                        :name        :password
                        :placeholder "Password"
                        :on-change   #(reset! password (-> % .-target .-value))
                        :required    true
                        :type        "password"
                        :value       @password}]
        [:> Divider {:horizontal true}]
        [:> Button {:primary  true
                    :type     :submit}
         "Submit"]
        [:> Button {:on-click #(rf/dispatch [::nav-to [:home]])}
         "Cancel"]]])))

(defn route-page
  [route]
  [:> Segment
   [:> Header {:as :h2} (route->text route)]])

(defn root-component
  []
  [:> Container
   [navbar]
   [kf/switch-route (fn [route] (-> route :data :name))
    :home [home-page]
    :drinks [route-page :drinks]
    :login [login-page]
    :logout [route-page :logout]
    :profile [route-page :profile]
    :register [route-page :register]
    nil [:div "Loading..."]]])

(defn mount-components
  ([] (mount-components true))
  ([debug?]
   (rf/clear-subscription-cache!)
   (kf/start! {:debug?         (boolean debug?)
               :hash-routing?  true
               :initial-db     initial-db
               :routes         routes
               :root-component [root-component]})))

(defn init!
  [debug?]
  (mount-components debug?))
