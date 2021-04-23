(ns rate-that-drink.routes.pages
  (:require
   [rate-that-drink.schema :as sc]
   [hiccup.def :refer [defelem]]
   [hiccup.form :refer [drop-down
                        email-field
                        label
                        password-field
                        text-area
                        text-field]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn head []
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:title "Rate That Drink"]
   [:link {:href "/assets/Semantic-UI/semantic.min.css"
           :rel "stylesheet"
           :type "text/css"}]
   [:link {:href "/css/screen.css"
           :rel "stylesheet"
           :type "text/css"}]
   [:link {:href "/css/datatables.min.css"
           :rel "stylesheet"
           :type "text/css"}]])

(defn base-page
  "The base page for the app"
  [content]
  [:html
   (head)
   [:body
    content
    [:script {:src "/assets/jquery/jquery.js"}]
    [:script {:src "/assets/Semantic-UI/semantic.min.js"}]
    [:script {:src "/js/datatables.min.js"}]
    [:script {:src "/js/app.js"}]]])

(defn navbar-item
  "An element representing a navbar link"
  [uri link title]
  [:div.item {:class (when (= uri link) "active")}
   [:a {:href link} title]])

(defn navbar
  "A navbar for the app"
  [request]
  (let [authed? (get-in request [:session :identity])
        item (partial navbar-item (:uri request))]
    [:div.ui.stackable.menu
     [:div.item
      [:img {:src "/img/beer.png"}]
      [:p.app-name "Rate That Drink"]]
     (if authed?
       (item "/user/drinks" "Drinks")
       (item "/" "Home"))
     (if authed?
       (item "/user/logout" "Log Out")
       (item "/login" "Log In"))
     (when (not authed?)
       (item "/profile" "Sign Up"))
     (when authed?
       [:div.item.right
        [:i.icon.user.circle]
        [:a {:href "/user/profile"} (get-in request [:session :first_name])]])]))

(defn error-page
  "A page for displaying errors"
  [{status :status title :title message :message}]
  [:html
   (head)
   [:div.ui.container
    [:h1.ui.header "Error " status]
    [:div.ui.error.message
     [:i.icon.large.exclamation.triangle]
     [:div.header {:role "header"} title]
     (when message [:p message])]]])

(defn home-page
  []
  [:div.ui.segment
   [:h1 "Welcome to the 'Rate that drink' app"]
   [:p "This is a place where you can rate and compare your favorite drink."]
   [:p "Login or sign up to get started!"]])

(defn form-errors [errors]
  (when (not-empty errors)
    [:div.ui.error.message
     (for [error errors]
       [:p error])]))

(defn login-page [{errors :errors}]
  [:div.ui.segment
   [:h1 "Enter your email and password"]
   (form-errors errors)
   [:form.ui.form {:method "post"}
    [:div.field
     (label "email" "Email")
     (email-field {:placeholder "Email" :required true} "email")]
    [:div.field
     (label "pass" "Password")
     (password-field {:placeholder "Password" :required true} "pass")]
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/"} "Cancel"]]])

(defn profile-form-page [{:keys [errors user]}]
  [:div.ui.segment
   (if user
     [:h1 "Update your account"]
     [:h1 "Create an account"])
   (form-errors errors)
   [:form.ui.form {:method "post"}
    [:div.field
     (label "first_name" "First Name")
     (text-field {:placeholder "First Name" :required true} "first_name" (:first_name user))]
    [:div.field
     (label "last_name" "Last Name")
     (text-field {:placeholder "Last Name" :required true} "last_name" (:last_name user))]
    [:div.field
     (label "email" "Email")
     (email-field {:placeholder "Email" :required true} "email" (:email user))]
    ;; TODO reset password
    (when-not user
      [:div.field
       (label "pass" "Password")
       (password-field {:placeholder "Password" :required true} "pass")])
    (when-not user
      [:div.field
       (label "confirm-pass" "Confirm Password")
       (password-field {:placeholder "Confirm Password" :required true} "confirm-pass")])
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/"} "Cancel"]]])

(defn user-beers-page [{:keys [beers]}]
  [:div.ui.segment
   [:div.beers-header
    [:h1 "Your Beers"]
    [:a {:href "/user/beers/new"} "Add New"]]
   [:table#beers.ui.sortable.selectable.celled.table
    [:thead
     [:tr
      [:th "Name"]
      [:th "Brewery"]
      [:th "Style"]
      [:th.one.wide "Rating"]]]
    [:tbody
     (for [{:keys [id
                   name
                   brewery
                   style] :as beer} beers]
       [:tr {:data-beer-id id}
        [:td {:data-label "Name"} name]
        [:td {:data-label "Brewery"} brewery]
        [:td {:data-label "Style"} style]
        [:td {:data-label "Rating"} (sc/beer-total beer)]])]]])


(defelem rating-field [name min max placeholder value]
  [:input.rating {:type "number"
                  :name name
                  :min min
                  :max max
                  :placeholder placeholder
                  :value value}])

(defn user-beer-form-page [{:keys [beer errors]}]
  [:div.ui.segment
   [:div.beers-header
    [:h1 (if beer "Update your beer" "Add a new beer")]
    (when beer
      [:a.ui.button.negative {:href (str "/user/beers/delete/" (:id beer))} "Delete"])]
   (form-errors errors)
   [:form.ui.form {:method "post"}
    [:div.field
     (label "name" "Name")
     (text-field {:maxlength 100 :placeholder "Beer Name" :required true} "name" (:name beer))]
    [:div.field
     (label "brewery" "Brewery")
     (text-field {:maxlength 100 :placeholder "Brewery" :required true} "brewery" (:brewery beer))]
    [:div.field
     (label "style" "Style")
     (text-field {:maxlength 100 :placeholder "Style" :required true} "style" (:style beer))]
    [:div.two.fields
     [:div.field
      (label "appearance" "Appearance (0-10)")
      (rating-field "appearance" 0 10 "Appearance" (:appearance beer))]
     [:div.field
      (label "smell" "Smell (0-10)")
      (rating-field "smell" 0 10 "Smell" (:smell beer))]]
    [:div.two.fields
     [:div.field
      (label "taste" "Taste (0-30)")
      (rating-field "taste" 0 30 "Taste" (:taste beer))]
     [:div.field
      (label "aftertaste" "Aftertaste (0-20)")
      (rating-field "aftertaste" 0 20 "Aftertaste" (:aftertaste beer))]]
    [:div.two.fields
     [:div.field.rating
      (label "drinkability" "Drinkability (0-30)")
      (rating-field "drinkability" 0 30 "Drinkability" (:drinkability beer))]
     [:div.field
      [:label "Total (0-100)"]
      [:p.total (sc/beer-total beer)]]]
    [:div.field
     (label "comments" "Comments")
     (text-area "comments" (:comments beer))]
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/user/beers"} "Cancel"]]])

;;
;; Drinks
;;

(defn user-drinks-page
  [{drinks :drinks}]
  [:div.ui.segment
   [:div.drinks-header
    [:h1 "Your Drinks"]
    [:a {:href "/user/drinks/new"} "Add New"]]
   [:table#drinks.ui.celled.selectable.table
    [:thead
     [:tr
      [:th "Name"]
      [:th "Maker"]
      [:th "Type"]
      [:th "Style"]
      [:th.one.wide "Rating"]]]
    [:tbody
     (for [{:keys [id
                   name
                   maker
                   type
                   style
                   rating]}
           drinks]
       [:tr {:data-drink-id id}
        [:td {:data-label "Name"}   name]
        [:td {:data-label "Maker"}  maker]
        [:td {:data-label "Type"}   type]
        [:td {:data-label "Style"}  style]
        [:td {:data-label "Rating"} (format "%.1f" rating)]])]]])

(defelem drink-rating-field [name min max placeholder value]
  [:input.drink-rating {:type "number"
                        :name name
                        :min min
                        :max max
                        :placeholder placeholder
                        :required true
                        :value value}])

(defelem drink-rating-section [field field-label drink]
  (let [field-name (name field)
        notes-field-name (str field-name "_notes")
        notes-field-key  (keyword notes-field-name)]
    [:div
     [:h4.ui.dividing.header field-label]
     [:div.two.fields
      [:div.field
       (label field-name "Rating (1-5)")
       (drink-rating-field field-name 1 5 field-label (field drink))]
      [:div.field
       (label notes-field-name "Notes")
       (text-area {:rows 3} notes-field-name (notes-field-key drink))]]]))

(defn user-drink-form-page [{:keys [drink errors]}]
  (prn drink)
  [:div.ui.segment
   [:div.drinks-header
    [:h1 (if drink "Update your drink" "Add a new drink")]
    (when drink
      [:a.ui.button.negative {:href (str "/user/drinks/delete/" (:id drink))} "Delete"])]
   (form-errors errors)
   [:form.ui.form {:method "post"}
    [:div.two.fields
     [:div.field
      (label "name" "Name")
      (text-field {:maxlength 100 :placeholder "Drink Name" :required true}
                  "name"
                  (:name drink))]
     [:div.field
      (label "brewery" "Maker")
      (text-field {:maxlength 100 :placeholder "Maker" :required true}
                  "maker"
                  (:maker drink))]]
    [:div.two.fields
     [:div.field
      (label "type" "Type")
      (drop-down {:class "ui fluid dropdown"}
                 "type"
                 [["Beer" "beer"]
                  ["Wine" "wine"]]
                 (:type drink))]
     [:div.field
      (label "style" "Style")
      (text-field {:maxlength 100 :placeholder "Style" :required true} "style" (:style drink))]]
    (drink-rating-section :appearance "Appearance" drink)
    (drink-rating-section :smell "Smell" drink)
    (drink-rating-section :taste "Taste" drink)
    [:div
     [:h4.ui.dividing.header "Overall"]
     [:div.two.fields
      [:div.field
       [:label "Rating (1-5)"]
       [:p {:name "total"} (sc/drink-total drink)]]
      [:div.field
       (label "comments" "Comments")
       (text-area {:rows 3} "comments" (:comments drink))]]]
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/user/drinks"} "Cancel"]]])

(defn delete-beer-page [{errors :errors id :id}]
  [:div.ui.segment
   [:h1 "Please confirm"]
   [:div.ui.warning.message
    [:p "Are you sure that you want to delete?"]]
   (when (not-empty errors)
     [:div.ui.error.message
      (for [error errors]
        [:p error])])
   [:form.ui.form {:method "post"}
    (anti-forgery-field)
    [:input {:hidden true :value id}]
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/user/beers"} "Cancel"]]])

(defn delete-drink-page [{errors :errors id :id}]
  [:div.ui.segment
   [:h1 "Please confirm"]
   [:div.ui.warning.message
    [:p "Are you sure that you want to delete?"]]
   (when (not-empty errors)
     [:div.ui.error.message
      (for [error errors]
        [:p error])])
   [:form.ui.form {:method "post"}
    (anti-forgery-field)
    [:input {:hidden true :value id}]
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href (str "/user/drinks/edit/" id)} "Cancel"]]])
