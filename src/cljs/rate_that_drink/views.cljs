(ns rate-that-drink.views
  (:require
   ["semantic-ui-react" :refer [Button
                                Container
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
   [rate-that-drink.common :as common]
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

(defrecord Column [key label mobile? width])

(def all-columns
  [(->Column :name       "Name"       true  nil)
   (->Column :maker      "Maker"      true  nil)
   (->Column :type       "Type"       false nil)
   (->Column :style      "Style"      false nil)
   (->Column :appearance "Appearance" false 1)
   (->Column :smell      "Smell"      false 1)
   (->Column :taste      "Taste"      false 1)
   (->Column :total      "Rating"     true  1)])

(defn drinks-table-menu
  [{:keys [page page-count]}]
  [:div {:style {:display         :flex
                 :flex-direction  :column
                 :justify-content :space-between}}
   [:div
    [:> Input {:class       "margin-right-sm"
               :icon        "search"
               :placeholder "Search..."
               :on-change   #(rf/dispatch
                              [::events/set-drinks-filter (-> % .-target .-value)])}]
    [:> Button
     {:on-click #(rf/dispatch [::events/nav-to [:new-drink]])}
     [:> Icon {:name "plus"}]
     "Add Drink"]]
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
      (for [{key :key} columns]
        ^{:key key}
        [:> Table.Cell
         (get drink key)])])])

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
   [:> Pagination {:active-page page
                   :boundary-range 1
                   :on-page-change (fn [_ pag]
                                     (rf/dispatch [::events/set-drinks-page
                                                   (-> pag js->clj (get "activePage"))]))
                   ;; :show-elipsis true
                   :sibling-range 1
                   :total-pages (count pages)}]])

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
  [{:keys [drink field label on-change]}]
  [:<>
   [:> Header {:as       :h4
               :dividing true}
    label]
   [:> Form.Group {:widths :two}
    [:> Form.Input {:default-value (get drink field)
                    :name          field
                    :label         "Rating (1-5)"
                    :max           5
                    :min           1
                    :on-change     (partial on-change field)
                    :placeholder   label
                    :required      true
                    :type          :number}]
    (let [notes-key (keyword (str (name field) "_notes"))]
      [:> Form.TextArea {:default-value (get drink notes-key)
                         :name          notes-key
                         :label         "Notes"
                         :on-change     (partial on-change notes-key)
                         :placeholder   "Notes"}])]])

(defn- to-number
  [s]
  (when s
    (js/Number. s)))

(defn ratings-to-number
  [drink]
  (-> drink
      (update :appearance to-number)
      (update :smell      to-number)
      (update :taste      to-number)))

(defn overall-rating-group
  [{:keys [drink on-change]}]
  [:<>
   [:> Header {:as       :h4
               :dividing true}
    "Overall"]
   [:> Form.Group {:widths :two}
    [:div.ui.field {:label "Rating (1-5)"}
     [:label "Rating (1-5)"]
     [:p (common/calculate-ratings-total drink)]]
    [:> Form.TextArea {:default-value (:comments drink)
                       :name          :comments
                       :label         "Comments"
                       :on-change     (partial on-change :comments)
                       :placeholder   "Comments"
                       :rows          3}]]])

(defn drink-page
  [{:keys [drink is-edit? on-cancel on-change on-select-change on-submit]}]
  [:> Segment
   [:> Header {:as :h1} (if is-edit?
                          "Edit drink"
                          "Add a new drink")]
   [:> Form {:on-submit on-submit}
    [:> Form.Group {:widths :two}
     [:> Form.Input {:default-value (:name drink)
                     :label         "Name"
                     :name          :name
                     :on-change     (partial on-change :name)
                     :placeholder   "Name"
                     :required      true}]
     [:> Form.Input {:default-value (:maker drink)
                     :label         "Maker"
                     :name          :maker
                     :on-change     (partial on-change :maker)
                     :placeholder   "Maker"
                     :required      true}]]
    [:> Form.Group {:widths :two}
     [:> Form.Select {:default-value (:type drink)
                      :label         "Type"
                      :name          :type
                      :on-change     (partial on-select-change :type)
                      :options       drink-type-options
                      :placeholder   "Type"
                      :search        true
                      :selection     true
                      :required      true}]
     [:> Form.Input {:default-value (:style drink)
                     :label         "Style"
                     :name          :style
                     :on-change     (partial on-change :style)
                     :placeholder   "Style"
                     :required      true}]]
    [drink-ratings-group {:drink drink
                          :field     :appearance
                          :label     "Appearance"
                          :on-change on-change}]
    [drink-ratings-group {:drink     drink
                          :field     :smell
                          :label     "Smell"
                          :on-change on-change}]
    [drink-ratings-group {:drink     drink
                          :field     :taste
                          :label     "Taste"
                          :on-change on-change}]
    [overall-rating-group {:drink     drink
                           :on-change on-change}]
    [:> Divider {:horizontal true}]
    [:> Button {:primary  true
                :type     :submit}
     "Submit"]
    [:> Button {:on-click on-cancel
                :type     :button}
     "Cancel"]]])

(defn new-drink-page
  []
  (let [drink (r/atom {})]
    (fn []
      [drink-page {:drink     @drink
                   :on-cancel (fn []
                                (rf/dispatch [::events/nav-to [:drinks]]))
                   :on-change (fn [field e]
                                (swap! drink #(-> %
                                                  (assoc field (-> e .-target .-value))
                                                  ratings-to-number)))
                   :on-select-change (fn [field _ d]
                                       (swap! drink assoc field (.-value d)))
                   :on-submit (fn [_]
                                (rf/dispatch [::events/create-drink @drink]))}])))

(defn edit-drink-page
  []
  (let [selected-drink @(rf/subscribe [::subs/selected-drink])
        drink          (r/atom (or selected-drink {}))]
    (fn []
      [drink-page {:drink     @drink
                   :is-edit?  true
                   :on-cancel (fn []
                                (rf/dispatch [::events/nav-to [:drinks]]))
                   :on-change (fn [field e]
                                (swap! drink #(-> %
                                                  (assoc field (-> e .-target .-value))
                                                  ratings-to-number)))
                   :on-select-change (fn [field _ d]
                                       (swap! drink assoc field (.-value d)))
                   :on-submit (fn [_]
                                (rf/dispatch [::events/edit-drink @drink]))}])))

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
      :logout     [route-page :logout]
      :new-drink  [new-drink-page]
      :profile    [edit-profile-page]
      :register   [create-profile-page]
      nil [:div "Loading..."]]]))
