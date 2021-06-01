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
   [rate-that-drink.routes :refer [route->text routes]]
   [rate-that-drink.subs :as subs]))

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

(defn- e->value
  [e]
  (-> e .-target .-value))

(defn get-value
  [atm field]
  (get @atm field ""))

(defn login-page
  []
  (let [email (r/atom "")
        password (r/atom "")
        credentials (r/atom {})]
    (fn []
      [:> Segment
       [:> Header {:as :h2} "Enter your email and password"]
       [:> Form {:on-submit #(rf/dispatch [::events/login @credentials])}
        [:> Form.Input {:label       "Email"
                        :name        :email
                        :placeholder "Email"
                        :on-change   #(swap! credentials assoc :email (e->value %))
                        :required    true
                        :type        "email"
                        :value       (get-value credentials :email)}]
        [:> Form.Input {:label       "Password"
                        :name        :password
                        :placeholder "Password"
                        :on-change   #(swap! credentials assoc :password (e->value %))
                        :required    true
                        :type        "password"
                        :value       (get-value credentials :password)}]
        [:> Divider {:horizontal true}]
        [:> Button {:primary  true
                    :type     :submit}
         "Submit"]
        [:> Button {:on-click #(rf/dispatch [::events/nav-to [:home]])
                    :type     :button}
         "Cancel"]]])))

(defn profile-page
  []
  (let [saved-user @(rf/subscribe [::subs/user])
        user       (r/atom saved-user)
        is-edit?   (not (nil? (:id saved-user)))]
    (fn []
      [:> Segment
       [:> Header {:as :h1} (if is-edit?
                              "Edit your profile"
                              "Create a new profile")]
       [:> Form {:on-submit (if is-edit?
                              #(rf/dispatch [::events/edit-profile @user])
                              #(rf/dispatch [::events/create-profile @user]))}
        [:> Form.Input {:label       "First Name"
                        :name        :first_name
                        :placeholder "First Name"
                        :on-change   #(swap! user assoc :first_name (e->value %))
                        :required    true
                        :value       (get-value user :first_name)}]
        [:> Form.Input {:label       "Last Name"
                        :name        :last_name
                        :placeholder "Last Name"
                        :on-change   #(swap! user assoc :last_name (e->value %))
                        :required    true
                        :value       (get-value user :last_name)}]
        [:> Form.Input {:label       "Email"
                        :name        :email
                        :placeholder "Email"
                        :on-change   #(swap! user assoc :email (e->value %))
                        :required    true
                        :type        "email"
                        :value       (get-value user :email)}]
        (when-not saved-user
          [:<>
           [:> Form.Input {:label       "Password"
                           :name        :password
                           :placeholder "Password"
                           :on-change   #(swap! user assoc :password (e->value %))
                           :required    true
                           :type        "password"
                           :value       (get-value user :password)}]
           [:> Form.Input {:label       "Confirm Password"
                           :name        :password-confirm
                           :placeholder "Confirm Password"
                           :on-change   #(swap! user assoc :password_confirm (e->value %))
                           :required    true
                           :type        "password"
                           :value       (get-value user :password_confirm)}]])
        [:> Divider {:horizontal true}]
        [:> Button {:primary  true
                    :type     :submit}
         "Submit"]
        [:> Button {:on-click (if is-edit?
                                #(rf/dispatch [::events/nav-to [:drinks]])
                                #(rf/dispatch [::events/nav-to [:home]]))
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
    :profile [profile-page]
    :register [route-page :register]
    nil [:div "Loading..."]]])
