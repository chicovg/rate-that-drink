(ns the-beer-tasting-app.routes.home
  (:require
   [clojure.java.io :as io]
   [ring.util.response]
   [ring.util.http-response :as response]
   [the-beer-tasting-app.layout :as layout]
   [the-beer-tasting-app.middleware :as middleware]
   [the-beer-tasting-app.db.core :as db])
  (:use [hiccup.def]))

(defn home-page [request]
  (layout/render request
                 [:div.ui.container.segment
                  [:h2 "Welcome to the beer tasting app"]
                  [:p "This is a place where you can rate and compare your favorite brews."]
                  [:p "Login or sign up to get started!"]]))

(defn login-page [request]
  (layout/render request [:h1 "Login"]))

(defn sign-up-page [request]
  (layout/render request [:h1 "Sign up"]))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/login" {:get login-page}]
   ["/signup" {:get sign-up-page}]])
