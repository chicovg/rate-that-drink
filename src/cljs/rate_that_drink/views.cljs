(ns rate-that-drink.views
  (:require
   ["semantic-ui-react" :refer [Button
                                Container
                                Dimmer
                                Divider
                                Form
                                Form.Input
                                Header
                                Loader
                                Menu
                                Menu.Item
                                Segment]]
   [clojure.string :as st]
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [rate-that-drink.events :as events]
   [rate-that-drink.routes :refer [route->text visible-routes]]
   [rate-that-drink.subs :as subs]))

(defn navbar
  []
  (let [profile    @(rf/subscribe [::subs/profile])
        logged-in? (not (nil? profile))
        routes     (visible-routes logged-in?)]
    [:> Menu {:stackable true}
     (for [{:keys [key label]} routes]
       ^{:key key} [:> Menu.Item
                    {:name (name key)
                     :on-click #(rf/dispatch [::events/nav-to [key]])}
                    label])]))

(defn home-page
  []
  [:> Segment
   [:> Header {:as "h1"} "Welcome to the 'Rate that drink' app"]
   [:p "This is a place where you can rate and compare your favorite drink."]
   [:p "Login or sign up to get started!"]])

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
  [{:keys [is-edit? profile on-cancel on-submit]}]
  [:> Segment
   [:> Header {:as :h1} (if is-edit?
                          "Edit your profile"
                          "Create a new profile")]
   [:> Form {:on-submit on-submit}
    [:> Form.Input {:default-value (:first_name profile)
                    :label         "First Name"
                    :name          :first_name
                    :placeholder   "First Name"
                    :required      true}]
    [:> Form.Input {:default-value (:last_name profile)
                    :label         "Last Name"
                    :name          :last_name
                    :placeholder   "Last Name"
                    :required      true}]
    [:> Form.Input {:default-value (:email profile)
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
    [:> Button {:on-click on-cancel
                :type     :button}
     "Cancel"]]])

(defn create-profile-page
  []
  [profile-page {:is-edit?  false
                 :profile   nil
                 :on-submit (fn [submit]
                              (rf/dispatch [::events/create-profile
                                            (values-from-submit
                                             [:email
                                              :first_name
                                              :last_name
                                              :password
                                              :password_confirm]
                                             submit)]))
                 :on-cancel (fn []
                              (rf/dispatch [::events/nav-to [:home]]))}])

(defn edit-profile-page
  []
  [profile-page {:is-edit?  true
                 :profile   @(rf/subscribe [::subs/profile])
                 :on-submit (fn [submit]
                              (rf/dispatch [::events/edit-profile
                                            (values-from-submit
                                             [:email
                                              :first_name
                                              :last_name]
                                             submit)]))
                 :on-cancel (fn []
                              (rf/dispatch [::events/nav-to [:drinks]]))}])

(defn route-page
  [route]
  [:> Segment
   [:> Header {:as :h2} (route->text route)]])

(defn root-component
  []
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [:> Container
     [navbar]
     [:> Dimmer {:active loading?
                 :inverted true}
      [:> Loader {:inverted true}
       "Loading"]]
     [kf/switch-route (fn [route] (-> route :data :name))
      :home [home-page]
      :drinks [route-page :drinks]
      :login [login-page]
      :logout [route-page :logout]
      :profile [edit-profile-page]
      :register [create-profile-page]
      nil [:div "Loading..."]]]))
