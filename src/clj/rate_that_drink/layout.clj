(ns rate-that-drink.layout
  (:require
   [clojure.java.io :as io]
   [hiccup.core :as h]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
   [ring.util.http-response :refer [content-type ok]]
   [ring.util.response]
   [rate-that-drink.routes.pages :as pages]
   [selmer.parser :as parser]))

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

(parser/set-resource-path! (io/resource "html"))

(defn render-html
  "renders the HTML template located relative to resources/html"
  [_ template & [params]]
  (content-type
    (ok
      (parser/render-file
        template
        (assoc params
          :page template
          :csrf-token *anti-forgery-token*)))
    "text/html; charset=utf-8"))
