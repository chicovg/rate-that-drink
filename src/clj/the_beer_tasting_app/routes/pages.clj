(ns the-beer-tasting-app.routes.pages
  (:require
   [the-beer-tasting-app.schema :as sc])
  (:use [hiccup.core]
        [hiccup.def]
        [hiccup.form]
        [ring.util.anti-forgery]))

(defn head []
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:title "Rate That Beer"]
   [:link {:href "/assets/Semantic-UI/semantic.min.css"
           :rel "stylesheet"
           :type "text/css"}]
   [:link {:href "/css/screen.css"
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
    [:script {:src "/js/tablesort.js"}]
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
      [:p.app-name "Rate that beer"]]
     (if authed?
       (item "/user/beers" "Beers")
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
   [:h1 "Welcome to the 'Rate that beer' app"]
   [:p "This is a place where you can rate and compare your favorite brews."]
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
     (email-field {:placeholder "Email" :required true} "email" {:email user})]
    ;; TODO reset password
    (when (not user)
      [:div.field
       (label "pass" "Password")
       (password-field {:placeholder "Password" :required true} "pass")]
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
