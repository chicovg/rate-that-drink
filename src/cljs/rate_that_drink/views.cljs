(ns rate-that-drink.views
  (:require
   ["semantic-ui-react" :refer [Button
                                Container
                                Dimmer
                                Divider
                                Form
                                Form.Input
                                Header
                                Icon
                                Input
                                Loader
                                Menu
                                Menu.Item
                                Message
                                Message.Header
                                Segment
                                Table
                                Table.Body
                                Table.Cell
                                Table.Footer
                                Table.Header
                                Table.HeaderCell
                                Table.Row]]
   [breaking-point.core :as bp]
   [clojure.string :as st]
   [kee-frame.core :as kf]
   [re-frame.core :as rf]
   [rate-that-drink.events :as events]
   [rate-that-drink.errors :as errors]
   [rate-that-drink.routes :refer [route->text visible-routes]]
   [rate-that-drink.subs :as subs]))

(defn navbar
  []
  (let [profile    @(rf/subscribe [::subs/profile])
        logged-in? (not (nil? profile))
        routes     (visible-routes logged-in?)]
    [:> Menu {:stackable true}
     [:> Menu.Item
      [:img {:src "/img/beer.png"}]
      [:p.app-name "Rate That Drink"]]
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

(defn error-message
  [{:keys [error-key on-dismiss]}]
  (when-let [{:keys [header message]} (get errors/error-texts error-key)]
    [:> Message {:error true
                 :on-dismiss on-dismiss}
     [:> Message.Header header]
     message]))

(defn login-page
  []
  (let [field-values [:email :password]
        on-submit (fn [submit]
                    (let [credentials (values-from-submit field-values submit)]
                      (rf/dispatch [::events/login credentials])))]
    [:> Segment
     [:> Header {:as :h2} "Enter your email and password"]
     [error-message {:error-key @(rf/subscribe [::subs/error :login])
                     :on-dismiss #(rf/dispatch [::events/set-error :login nil])}]
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

;; TODO fancy table shit
;; need the following state in db
;; ::db/drinks-table-params {:page
;;                           :filter
;;                           :sort {:field
;;                                  :asc? T/F}}
;; - sort
;; - edit items
;;
;; -- table actions modify route.
;;     - use nav-to?

(defrecord Column [key label mobile?])

(def all-columns
  [(->Column :name       "Name"       true)
   (->Column :maker      "Maker"      true)
   (->Column :type       "Type"       false)
   (->Column :style      "Style"      false)
   (->Column :appearance "Appearance" false)
   (->Column :smell      "Smell"      false)
   (->Column :taste      "Taste"      false)
   (->Column :total      "Rating"     true)])

(defn drinks-table-menu
  [{:keys [page page-count]}]
  [:div {:style {:display         :flex
                 :justify-content :space-between}}
   [:p (str "Page " (inc page) " of " page-count)]
   [:> Input {:icon        "search"
              :placeholder "Search..."
              :on-change   #(rf/dispatch [::events/set-drinks-filter
                                          (-> % .-target .-value)])}]])

(defn drinks-table-header
  [{:keys [columns]}]
  [:> Table.Header
   [:> Table.Row
    (for [{:keys [key label]} columns]
      ^{:key key}
      [:> Table.HeaderCell label])]])

(defn drinks-table-body
  [{:keys [columns drinks]}]
  [:> Table.Body
   (for [drink drinks]
     ^{:key (:id drink)}
     [:> Table.Row
      (for [{key :key} columns]
        ^{:key key}
        [:> Table.Cell {:on-click #(prn (:id drink))}
         (get drink key)])])])

(defn drinks-table-footer
  [{:keys [col-count page pages]}]
  [:> Table.Footer
   [:> Table.Row
    [:> Table.HeaderCell {:colSpan col-count}
     [:> Menu {:floated "right"}
      [:> Menu.Item {:as "a"}
       [:> Icon {:name "chevron left"}]]
      (for [p pages]
        ^{:key p} [:> Menu.Item {:active (= p page)
                                 :as "a"}
                   (inc p)])
      [:> Menu.Item {:as "a"}
       [:> Icon {:name "chevron right"}]]]]]])

(defn drinks-table
  [{:keys [columns drinks]}]
  [:> Table {:compact    true
             :selectable true
             :stackable  true
             :striped    true}
   [drinks-table-header {:columns columns}]
   [drinks-table-body   {:columns columns
                         :drinks  drinks}]])

(defn drinks-table-footer-menu
  [{:keys [page pages]}]
  [:div {:style {:display "flex"
                 :justify-content "flex-end"}}
   [:> Menu
    [:> Menu.Item {:as "a"
                   :disabled (zero? page)
                   :on-click #(rf/dispatch [::events/dec-drinks-page])}
     [:> Icon {:name "chevron left"}]]
    (for [p pages]
      ^{:key p} [:> Menu.Item {:active (= p page)
                               :as "a"
                               :on-click #(rf/dispatch [::events/set-drinks-page p])}
                 (inc p)])
    [:> Menu.Item {:as "a"
                   :disabled (= page (last pages))
                   :on-click #(rf/dispatch [::events/inc-drinks-page])}
     [:> Icon {:name "chevron right"}]]]])

(defn drinks-page
  []
  (let [is-mobile? @(rf/subscribe [::bp/mobile?])
        columns    (->> all-columns
                        (filter #(or (not is-mobile?)
                                     (:mobile? %))))
        drinks     @(rf/subscribe [::subs/paginated-drinks])
        page       @(rf/subscribe [::subs/drinks-page])
        page-count @(rf/subscribe [::subs/drinks-page-count])
        pages      @(rf/subscribe [::subs/drinks-pages])]
    [:> Segment
     [:> Header {:as :h2} "Your Drinks"]
     [drinks-table-menu {:page       page
                         :page-count page-count}]
     [drinks-table {:columns columns
                    :drinks  drinks
                    :page    page
                    :pages   pages}]
     [drinks-table-footer-menu {:page  page
                                :pages pages}]]))

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
      [:> Loader {:inverted true} "Loading"]]
     [kf/switch-route (fn [route] (-> route :data :name))
      :home [home-page]
      :drinks [drinks-page]
      :login [login-page]
      :logout [route-page :logout]
      :profile [edit-profile-page]
      :register [create-profile-page]
      nil [:div "Loading..."]]]))
