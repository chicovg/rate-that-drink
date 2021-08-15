(ns rate-that-drink.routes.home
  (:require
   [rate-that-drink.layout :as layout]
   [rate-that-drink.middleware :as middleware]))

(defn home-page [request]
  (layout/render-html request "home.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]])
