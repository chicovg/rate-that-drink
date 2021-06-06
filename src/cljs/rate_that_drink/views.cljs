(ns rate-that-drink.views
  (:require
   ["semantic-ui-react" :refer [Button
                                Container
                                Divider
                                Form
                                Form.Input
                                Header
                                Menu
                                Menu.Item
                                Segment]]
   [clojure.string :as st]
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

(defn nil-if-blank
  [s]
  (when-not (st/blank? s) s))

(defn values-from-submit
  [fields submit-event]
  (let [named-coll (-> submit-event
                       .-target
                       .-elements)]
    (into {} (for [f fields
                   :let [item (some-> named-coll (.namedItem (name f)))]
                   :when (not (nil? item))]
               [f (-> item .-value nil-if-blank)]))))

(defn login-page
  []
  (let [field-values [:email :password]
        on-submit (fn [submit]
                    (let [credentials (values-from-submit field-values submit)]
                      (rf/dispatch [::events/login credentials])))]
    [:> Segment
     [:> Header {:as :h2} "Enter your email and password"]
     [:> Form {:on-submit on-submit}
      [:> Form.Input {:label       "Email"
                      :name        :email
                      :placeholder "Email"
                      :required    true
                      :type        "email"}]
      [:> Form.Input {:label       "Password"
                      :name        :password
                      :placeholder "Password"
                      :required    true
                      :type        "password"}]
      [:> Divider {:horizontal true}]
      [:> Button {:primary  true
                  :type     :submit}
       "Submit"]
      [:> Button {:on-click #(rf/dispatch [::events/nav-to [:home]])
                  :type     :button}
       "Cancel"]]]))

(defn profile-page
  []
  (let [saved-user      @(rf/subscribe [::subs/user])
        is-edit?        (not (nil? (:id saved-user)))
        field-names     [:first_name
                         :last_name
                         :email
                         :password
                         :password_confirm]
        on-submit (fn [submit]
                    (let [user (values-from-submit field-names submit)]
                      (if is-edit?
                        (rf/dispatch [::events/edit-profile user])
                        (rf/dispatch [::events/create-profile user])))
                    (prn (values-from-submit field-names submit)))]
    [:> Segment
     [:> Header {:as :h1} (if is-edit?
                            "Edit your profile"
                            "Create a new profile")]
     [:> Form {:on-submit on-submit}
      [:> Form.Input {:default-value (:first_name saved-user)
                      :label         "First Name"
                      :name          :first_name
                      :placeholder   "First Name"
                      :required      true}]
      [:> Form.Input {:default-value (:last_name saved-user)
                      :label         "Last Name"
                      :name          :last_name
                      :placeholder   "Last Name"
                      :required      true}]
      [:> Form.Input {:default-value (:email saved-user)
                      :label         "Email"
                      :name          :email
                      :placeholder   "Email"
                      :required      true
                      :type          "email"}]
      (when-not is-edit?
        [:<>
         [:> Form.Input {:label       "Password"
                         :name        :password
                         :placeholder "Password"
                         :required    true
                         :type        "password"}]
         [:> Form.Input {:label       "Confirm Password"
                         :name        :password_confirm
                         :placeholder "Confirm Password"
                         :required    true
                         :type        "password"}]])
      [:> Divider {:horizontal true}]
      [:> Button {:primary  true
                  :type     :submit}
       "Submit"]
      [:> Button {:on-click (if is-edit?
                              #(rf/dispatch [::events/nav-to [:drinks]])
                              #(rf/dispatch [::events/nav-to [:home]]))
                  :type     :button}
       "Cancel"]]]))

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
