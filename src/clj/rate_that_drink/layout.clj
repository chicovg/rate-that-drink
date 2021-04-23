(ns rate-that-drink.layout
  (:require
   [hiccup.core :as h]
   [ring.util.http-response :refer [content-type ok]]
   [ring.util.response]
   [rate-that-drink.routes.pages :as pages]))

(defn render
  [request content]
  (content-type
   (ok (h/html (pages/base-page [:div.ui.container (pages/navbar request) content])))
   "text/html; charset=utf-8"))

(defn render-error
  "error should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [details]
  (content-type
   {:status (:status details)
    :body (h/html (pages/error-page details))}
   "text/html; charset=utf-8"))
