(ns the-beer-tasting-app.dev-middleware
  (:require
    [ring.middleware.reload :refer [wrap-reload]]
    [selmer.middleware :refer [wrap-error-page]]
    [prone.middleware :refer [wrap-exceptions]]))

(defn wrap-request-log [handler]
  (fn
    ([request]
     (do (prn request)
         (handler request)))
    ([request respond raise]
     (do (prn request)
         (handler request respond raise)))))

(defn wrap-dev [handler]
  (-> handler
      wrap-reload
      wrap-error-page
      wrap-request-log
      (wrap-exceptions {:app-namespaces ['the-beer-tasting-app]})))
