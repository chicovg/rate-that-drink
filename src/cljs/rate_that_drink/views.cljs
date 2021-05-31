(ns rate-that-drink.views
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
   [rate-that-drink.events :as events]
   [rate-that-drink.routes :refer [route->text routes]]))

(defn navbar
  []
  [:> Menu {:stackable true}
   (for [[_ route] routes]
     ^{:key route} [:> Menu.Item
                    {:name (name route)
                     :on-click #(rf/dispatch [::events/nav-to [route]])}
                    (route->text route)])])

(defn home-page
  []
  [:> Segment
   [:> Header {:as "h1"} "Welcome to the 'Rate that drink' app"]
   [:p "This is a place where you can rate and compare your favorite drink."]
   [:p "Login or sign up to get started!"]])

(defn login-page
  []
  (let [email (r/atom "")
        password (r/atom "")]
    (fn []
      [:> Segment
       [:> Header {:as :h2} "Enter your email and password"]
       [:> Form {:on-submit #(rf/dispatch [::events/login {:email @email
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
        [:> Button {:on-click #(rf/dispatch [::events/nav-to [:home]])
                    :type     :button}
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
