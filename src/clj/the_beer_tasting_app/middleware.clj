(ns the-beer-tasting-app.middleware
  (:require
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [buddy.auth.accessrules :refer [restrict]]
   [buddy.auth :refer [authenticated?]]
   [buddy.auth.backends.session :refer [session-backend]]
   [cheshire.generate :as cheshire]
   [cognitect.transit :as transit]
   [clojure.tools.logging :as log]
   [muuntaja.middleware :refer [wrap-format wrap-params]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.adapter.undertow.middleware.session :refer [wrap-session]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.middleware.flash :refer [wrap-flash flash-request flash-response]]
   [ring.util.response :refer [redirect]]
   [the-beer-tasting-app.env :refer [defaults]]
   [the-beer-tasting-app.layout :refer [render render-error]]
   [the-beer-tasting-app.middleware.formats :as formats]
   [the-beer-tasting-app.config :refer [env]]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (render-error {:status 500
                       :title "Something very bad has happened!"
                       :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
   handler
   {:error-response
    (render-error
     {:status 403
      :title "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn on-error [request response]
  (redirect "/login"))

(defn wrap-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

(defn wrap-auth [handler]
  (let [backend (session-backend)]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-auth
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))
      wrap-internal-error))
