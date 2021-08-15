(ns rate-that-drink.views
  (:require
   ["semantic-ui-react" :refer [Button
                                Container
                                Confirm
                                Dimmer
                                Divider
                                Form
                                Form.Group
                                Form.Input
                                Form.Select
                                Form.TextArea
                                Header
                                Icon
                                Input
                                Loader
                                Menu
                                Menu.Item
                                Message
                                Message.Header
                                Pagination
                                Popup
                                Popup.Content
                                Popup.Header
                                Rating
                                Segment
                                Table
                                Table.Body
                                Table.Cell
                                Table.Header
                                Table.HeaderCell
                                Table.Row]]
   [breaking-point.core :as bp]
   [clojure.string :as st]
   [kee-frame.core :as kf]
   [reagent.core :as r]
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

(defn logout-page
  []
  [:div
   [:> Confirm {:header     "Log out"
                :content    "Are you sure that you want to log out?"
                :open       true
                :on-cancel  #(rf/dispatch [::events/nav-to [:drinks]])
                :on-confirm #(rf/dispatch [::events/logout])}]])

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
  (let [profile @(rf/subscribe [::subs/profile])]
    [profile-page {:is-edit?  true
                   :profile   profile
                   :on-submit (fn [submit]
                                (rf/dispatch [::events/edit-profile
                                              (merge {:id (:id profile)}
                                                     (values-from-submit
                                                      [:email
                                                       :first_name
                                                       :last_name]
                                                      submit))]))
                   :on-cancel (fn []
                                (rf/dispatch [::events/nav-to [:drinks]]))}]))

(defn rating-comp
  [label rating-str]
  [:> Popup
   {:trigger
    (r/as-element
     [:> Rating {:disabled       true
                 :icon           "star"
                 :max-rating     5
                 :rating         rating-str
                 :size           "mini"}])}
   [:> Popup.Header label]
   [:> Popup.Content
    [:span rating-str]]])

(defrecord Column [key label component mobile? width])

(def all-columns
  [(->Column :name       "Name"       nil         true  nil)
   (->Column :maker      "Maker"      nil         true  nil)
   (->Column :type       "Type"       nil         false nil)
   (->Column :style      "Style"      nil         false nil)
   (->Column :appearance "Appearance" rating-comp false 1)
   (->Column :smell      "Smell"      rating-comp false 1)
   (->Column :taste      "Taste"      rating-comp false 1)
   (->Column :total      "Rating"     rating-comp true  1)])

(defn drinks-filter-input
  []
  (let [filter      @(rf/subscribe [::subs/drinks-filter])
        has-filter? (pos? (count filter))]
    [:> Input {:action        has-filter?
               :class         ["margin-right-sm" "margin-top-sm"]
               :icon-position "left"}
     [:> Icon {:name "search"}]
     [:input {:on-change   #(rf/dispatch [::events/set-drinks-filter (-> % .-target .-value)])
              :placeholder "Search"
              :value       filter}]
     (when has-filter?
       [:> Button {:basic    true
                   :icon "delete"
                   :on-click #(rf/dispatch [::events/set-drinks-filter ""])}])]))

(defn drinks-table-menu
  [{:keys [page page-count]}]
  [:div {:style {:display         :flex
                 :flex-direction  :column
                 :justify-content :space-between}}
   [:div.flex-horizontal-wrap
    [drinks-filter-input]
    [:div.margin-top-sm
     [:> Button
      {:on-click #(rf/dispatch [::events/nav-to [:new-drink]])}
      [:> Icon {:name "plus"}]
      "Add Drink"]]]
   [:p {:class "margin-top-md"} (str "Page " page " of " page-count)]])

(defn drinks-table-header
  [{:keys [columns sort]}]
  [:> Table.Header
   [:> Table.Row
    (for [{:keys [key label width]} columns]
      ^{:key key}
      [:> Table.HeaderCell
       {:on-click #(rf/dispatch [::events/set-drinks-sort key])
        :sorted   (when (= key (:field sort)) (:direction sort))
        :style    {:cursor :pointer}
        :width    width}
       label])]])

(defn drinks-table-body
  [{:keys [columns drinks]}]
  [:> Table.Body
   (for [drink drinks]
     ^{:key (:id drink)}
     [:> Table.Row
      {:on-click #(rf/dispatch [::events/nav-to [:edit-drink {:id (:id drink)}]])}
      (for [{:keys [component key label]} columns]
        ^{:key key}
        [:> Table.Cell
         (if component
           [component label (get drink key)]
           (get drink key))])])])

(defn drinks-table
  [{:keys [columns drinks sort]}]
  [:> Table {:compact    true
             :selectable true
             :sortable   true
             :stackable  true
             :striped    true}
   [drinks-table-header {:columns columns
                         :sort    sort}]
   [drinks-table-body   {:columns columns
                         :drinks  drinks}]])

(defn drinks-table-footer-menu
  [{:keys [page pages]}]
  [:div {:style {:display "flex"
                 :justify-content "flex-end"}}
   [:> Pagination {:active-page    page
                   :boundary-range 1
                   :first-item     nil
                   :last-item      nil
                   :on-page-change (fn [_ pag]
                                     (rf/dispatch [::events/set-drinks-page
                                                   (-> pag js->clj (get "activePage"))]))
                   :sibling-range  1
                   :size           "mini"
                   :total-pages    (count pages)}]])

(defn drinks-page
  []
  (let [is-mobile? @(rf/subscribe [::bp/mobile?])
        columns    (->> all-columns
                        (filter #(or (not is-mobile?)
                                     (:mobile? %))))
        drinks     @(rf/subscribe [::subs/paginated-drinks])
        page       @(rf/subscribe [::subs/drinks-page])
        page-count @(rf/subscribe [::subs/drinks-page-count])
        pages      @(rf/subscribe [::subs/drinks-pages])
        sort       @(rf/subscribe [::subs/drinks-sort])]
    [:> Segment
     [:> Header {:as :h2} "Your Drinks"]
     [drinks-table-menu {:page       page
                         :page-count page-count}]
     [drinks-table {:columns columns
                    :drinks  drinks
                    :sort    sort}]
     [drinks-table-footer-menu {:page  page
                                :pages pages}]]))

(def drink-type-options
  [{:key :beer  :text "Beer" :value "beer"}
   {:key :cider :text "Cider" :value "cider"}
   {:key :mead  :text "Mead"  :value "mead"}
   {:key :wine  :text "Wine"  :value "wine"}
   {:key :other :text "Other" :value "other"}])

(defn drink-ratings-group
  [{:keys [drink
           editable?
           field
           label
           notes-field
           on-field-change]
    :or   {editable?   true
           notes-field (keyword (str (name field) "_notes"))}}]
  [:<>
   [:div.field
    [:> Header {:as       :h4
                :dividing true}
     label]
    [:label {:for field} "Rating"]
    [:div.flex-horizontal-align-items-center
     [:> Rating {:class       "margin-right-sm"
                 :disabled    (not editable?)
                 :rating      (get drink field)
                 :icon        "star"
                 :max-rating  5
                 :name        field
                 :on-rate     (fn [_ value]
                                (on-field-change field (.-rating value)))}]
     [:p "(" (get drink field) ")"]]
    [:> Form.TextArea {:name          notes-field
                       :label         "Notes"
                       :on-change     #(on-field-change notes-field (-> % .-target .-value))
                       :placeholder   "Notes"
                       :default-value (get drink notes-field)}]]])

(defn- to-number
  [s]
  (when s (-> s (js/Number.) (.valueOf))))

(defn- blank-to-nil
  [s]
  (when-not (st/blank? s) s))

(defn update-fields
  [drink]
  (-> drink
      (update :appearance       to-number)
      (update :smell            to-number)
      (update :taste            to-number)
      (update :appearance-notes blank-to-nil)
      (update :smell-notes      blank-to-nil)
      (update :taste-notes      blank-to-nil)
      (update :comments         blank-to-nil)))

(defn drink-page
  [{:keys [is-edit? on-cancel on-submit]}]
  (let [drink           @(rf/subscribe [::subs/selected-drink])
        on-field-change (fn [field value]
                          (rf/dispatch [::events/set-selected-drink
                                        (update-fields (assoc drink field value))]))]
    [:> Segment
     [:> Header {:as :h1} (if is-edit?
                            "Edit drink"
                            "Add a new drink")]
     [:> Form {:on-submit #(on-submit drink)}
      [:> Form.Group {:widths :two}
       [:> Form.Input {:default-value (:name drink)
                       :label         "Name"
                       :name          :name
                       :on-change     #(on-field-change :name (-> % .-target .-value))
                       :placeholder   "Name"
                       :required      true}]
       [:> Form.Input {:default-value (:maker drink)
                       :label         "Maker"
                       :name          :maker
                       :on-change     #(on-field-change :maker (-> % .-target .-value))
                       :placeholder   "Maker"
                       :required      true}]]
      [:> Form.Group {:widths :two}
       [:> Form.Select {:label       "Type"
                        :name        :type
                        :on-change   #(on-field-change :type (.-value %2))
                        :options     drink-type-options
                        :placeholder "Type"
                        :search      true
                        :selection   true
                        :required    true
                        :value       (:type drink)}]
       [:> Form.Input {:label       "Style"
                       :name        :style
                       :on-change    #(on-field-change :style (-> % .-target .-value))
                       :placeholder "Style"
                       :required    true
                       :default-value       (:style drink)}]]
      [:> Form.Group {:widths :two}
       [drink-ratings-group {:drink           drink
                             :field           :appearance
                             :label           "Appearance"
                             :on-field-change on-field-change}]
       [drink-ratings-group {:drink           drink
                             :field           :smell
                             :label           "Smell"
                             :on-field-change on-field-change}]]
      [:> Form.Group {:widths :two}
       [drink-ratings-group {:drink           drink
                             :field           :taste
                             :label           "Taste"
                             :on-field-change on-field-change}]
       [drink-ratings-group {:drink           drink
                             :editable?       false
                             :field           :total
                             :label           "Overall"
                             :notes-field     :comments
                             :on-field-change on-field-change}]]
      [:> Divider {:horizontal true}]
      [:> Button {:primary  true
                  :type     :submit}
       "Submit"]
      [:> Button {:on-click on-cancel
                  :type     :button}
       "Cancel"]]]))

(defn new-drink-page
  []
  [drink-page {:on-cancel (fn []
                            (rf/dispatch [::events/nav-to [:drinks]]))
               :on-submit (fn [drink]
                            (rf/dispatch [::events/create-drink drink]))}])

(defn edit-drink-page
  []
  [drink-page {:is-edit?  true
               :on-cancel (fn []
                            (rf/dispatch [::events/nav-to [:drinks]]))
               :on-submit (fn [drink]
                            (rf/dispatch [::events/edit-drink drink]))}])

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
      :home       [home-page]
      :edit-drink [edit-drink-page]
      :drinks     [drinks-page]
      :login      [login-page]
      :logout     [logout-page]
      :new-drink  [new-drink-page]
      :profile    [edit-profile-page]
      :register   [create-profile-page]
      nil [:div "Loading..."]]]))
