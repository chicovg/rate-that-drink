(ns the-beer-tasting-app.routes.home
  (:require
   [clojure.java.io :as io]
   [ring.util.response :refer [redirect bad-request]]
   [ring.util.http-response :as response]
   [the-beer-tasting-app.layout :as layout]
   [the-beer-tasting-app.middleware :as middleware]
   [the-beer-tasting-app.db.core :as db]
   [the-beer-tasting-app.schema :as sc]
   [struct.core :as s])
  (:use [ring.util.anti-forgery]))

(defn home-page [request]
  (layout/render request
                 [:div.ui.segment
                  [:h2 "Welcome to the beer tasting app"]
                  [:p "This is a place where you can rate and compare your favorite brews."]
                  [:p "Login or sign up to get started!"]]))

(defn login-page [request]
  (layout/render request
                 [:div.ui.segment
                  [:h1 "Login!"]]))

(defn authenticate-user [request]
  ;; TODO
  )

(defn profile-page [{errors :errors}]
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

(defn get-profile [request]
  (layout/render request (profile-page {:errors []})))

(defn create-profile [request]
  (prn request)
  (let [user (:params request)
        session (:session request)
        next-url (get-in request [:params :next] "/")]
    (let [schema-errors (s/validate user sc/user-schema)]
      (if (nil? (first schema-errors))
        (if (= (:pass user) (:confirm-pass user))
          (let [{id :id} (db/create-user! user)
                updated-session (assoc session :identity id)]
            (-> (redirect next-url)
                (assoc :session updated-session)))
          (layout/render request (profile-page {:errors ["Passwords do not match"]})))
        (let [errors (for [[field message] (first schema-errors)]
                       (str (name field) " " message))]
          (layout/render request (profile-page {:errors errors})))))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/login" {:get login-page
              :post authenticate-user}]
   ["/profile" {:get get-profile
                :post create-profile}]])
