(ns rate-that-drink.layout
  (:require
   [clojure.java.io :as io]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
   [ring.util.http-response :refer [content-type ok]]
   [ring.util.response]
   [selmer.parser :as parser]))

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
