(ns the-beer-tasting-app.handler
  (:require
    [the-beer-tasting-app.middleware :as middleware]
    [the-beer-tasting-app.layout :refer [render-error]]
    [the-beer-tasting-app.routes.home :refer [home-routes]]
    [the-beer-tasting-app.routes.user :refer [user-routes]]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [the-beer-tasting-app.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
     [(home-routes)
      (user-routes)])
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type
        (wrap-webjars (constantly nil)))
      (ring/create-default-handler
        {:not-found
         (constantly (render-error {:status 404, :title "404 - Page not found"}))
         :method-not-allowed
         (constantly (render-error {:status 405, :title "405 - Not allowed"}))
         :not-acceptable
         (constantly (render-error {:status 406, :title "406 - Not acceptable"}))}))))

(defn app []
  (middleware/wrap-base #'app-routes))
