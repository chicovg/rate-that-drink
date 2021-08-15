(ns rate-that-drink.handler
  (:require
    [rate-that-drink.middleware :as middleware]
    [rate-that-drink.routes.home :refer [home-routes]]
    [rate-that-drink.routes.services :refer [service-routes]]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [ring.util.http-response :refer [method-not-allowed
                                     not-found
                                     not-acceptable]]
    [rate-that-drink.env :refer [defaults]]
    [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
     [(home-routes)
      (service-routes)])
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type
        (wrap-webjars (constantly nil)))
      (ring/create-default-handler
        {:not-found
         (constantly (not-found "404 - Page not found"))
         :method-not-allowed
         (constantly (method-not-allowed "405 - Not allowed"))
         :not-acceptable
         (constantly (not-acceptable "406 - Not acceptable"))}))))

(defn app []
  (middleware/wrap-base #'app-routes))
