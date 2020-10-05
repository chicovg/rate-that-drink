(ns the-beer-tasting-app.routes.home
  (:require
   [buddy.hashers :as h]
   [ring.util.response :refer [redirect bad-request]]
   [the-beer-tasting-app.db.core :refer [*db*] :as db]
   [the-beer-tasting-app.layout :as layout]
   [the-beer-tasting-app.middleware :as middleware]
   [the-beer-tasting-app.schema :as sc])
  (:use [ring.util.anti-forgery]))

(defn home-page [request]
  (layout/render request
                 [:div.ui.segment
                  [:h2 "Welcome to the beer tasting app"]
                  [:p "This is a place where you can rate and compare your favorite brews."]
                  [:p "Login or sign up to get started!"]]))

(defn login-page [{errors :errors}]
  [:div.ui.segment
   [:h1 "Enter your email and password"]
   (when (not-empty errors)
     [:div.ui.error.message
      (for [error errors]
        [:p error])])
   [:form.ui.form {:method "post"}
    [:div.field
     [:label "Email"]
     [:input {:type "email" :name "email" :placeholder "Email" :required true}]]
    [:div.field
     [:label "Password"]
     [:input {:type "password" :name "pass" :placeholder "Password" :required true}]]
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/"} "Cancel"]]])

(defn get-login-page [request]
  (layout/render request (login-page {})))

(defn authenticate-user [request]
  (let [{email :email pass :pass} (:params request)
        user (db/get-user-by-email *db* {:email email})]
    (if (h/check pass (:pass user))
      (-> (redirect "/user/beers")
          (assoc-in [:session] (-> (:session request)
                                   (assoc :identity (:id user))
                                   (assoc :first_name (:first_name user)))))
      (layout/render request (login-page {:errors ["Invalid email or password"]})))))

(defn profile-form-page [{errors :errors}]
  [:div.ui.segment
   [:h1 "Create an account"]
   (when (not-empty errors)
     [:div.ui.error.message
      (for [error errors]
        [:p error])])
   [:form.ui.form {:method "post"}
    [:div.field
     [:label "First Name"]
     [:input {:type "text" :name "first_name" :placeholder "First Name" :required true}]]
    [:div.field
     [:label "Last Name"]
     [:input {:type "text" :name "last_name" :placeholder "Last Name" :required true}]]
    [:div.field
     [:label "Email"]
     [:input {:type "email" :name "email" :placeholder "Email" :required true}]]
    [:div.field
     [:label "Password"]
     [:input {:type "password" :name "pass" :placeholder "Password"}]]
    [:div.field
     [:label "Confirm Password"]
     [:input {:type "password" :name "confirm-pass" :placeholder "Password"}]]
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/"} "Cancel"]]])

(defn get-profile-form-page
  ([request] (layout/render request (profile-form-page {:errors []})))
  ([request errors] (layout/render request (profile-form-page {:errors errors}))))

(defn create-profile [request]
  (let [user (:params request)
        session (:session request)
        next-url (get-in request [:params :next] "/user/beers")]
    (let [schema-errors (sc/get-schema-errors user sc/user-schema)
          user-by-email (db/get-user-by-email user)]
      (if (empty? schema-errors)
        (if (not user-by-email)
          (if (= (:pass user) (:confirm-pass user))
            (let [encrypted-pass (h/encrypt (:pass user))
                  {id :id} (db/create-user! (assoc user :pass encrypted-pass))
                  updated-session (assoc session :identity id)]
              (-> (redirect next-url)
                  (assoc :session updated-session)))
            (get-profile-form-page request ["Passwords do not match"]))
          (get-profile-form-page request ["A user already exists with that email address"]))
        (get-profile-form-page request schema-errors)))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/login" {:get get-login-page
              :post authenticate-user}]
   ["/profile" {:get get-profile-form-page
                :post create-profile}]])
