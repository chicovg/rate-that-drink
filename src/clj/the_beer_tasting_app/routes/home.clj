(ns the-beer-tasting-app.routes.home
  (:require
   [the-beer-tasting-app.layout :as layout]
   [the-beer-tasting-app.db.core :as db]
   [clojure.java.io :as io]
   [the-beer-tasting-app.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render request "about.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]])

