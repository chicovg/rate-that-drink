(ns the-beer-tasting-app.layout
  (:require
   [clojure.java.io]
   [selmer.parser :as parser]
   [selmer.filters :as filters]
   [markdown.core :refer [md-to-html-string]]
   [ring.util.http-response :refer [content-type ok]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
   [ring.util.response])
  (:use [hiccup.def]))

(parser/set-resource-path!  (clojure.java.io/resource "html"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(defhtml base-page
  "The base page for the app"
  [content]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "The Beer Tasting App"]
    [:link {:href "/assets/Semantic-UI/semantic.min.css"
            :rel "stylesheet"
            :type "text/css"}]
    [:link {:href "/css/screen.css"
            :rel "stylesheet"
            :type "text/css"}]]
   [:body
    content
    [:script {:src "/assets/jquery/jquery.js"}]
    [:script {:src "/assets/Semantic-UI/semantic.min.js"}]
    [:script {:src "/js/app.js"}]]])

(defn navbar-item [uri link title]
  [:div.item {:class (when (= uri link) "active")}
   [:a {:href link} title]])

(defn navbar
  "A navbar for the app"
  [request]
  (let [authed? (get-in request [:session :identity])
        item (partial navbar-item (:uri request))]
    [:div.ui.stackable.menu
     [:div.item
      [:img {:src "/img/beer.png"}]
      [:p.app-name "Rate that beer"]]
     (if authed?
       (item "/user/beers" "Beers")
       (item "/" "Home"))
     (if authed?
       (item "/user/logout" "Log Out")
       (item "/login" "Log In"))
     (when (not authed?)
       (item "/profile" "Sign Up"))
     (when authed?
       [:div.item.right
        [:i.icon.user.circle]
        [:a {:href "/user/profile"} (get-in request [:session :first_name])]])]))

(defhtml error-page
  "A page for displaying errors"
  [{status :status title :title message :message}]
  (base-page
   [:div.ui.container
    [:h1.ui.header "Error " status]
    [:div.ui.error.message
     [:i.icon.large.exclamation.triangle]
     [:div.header {:role "header"} title]
     (when message [:p message])]]))

(defn render
  [request content]
  (content-type
   (ok (base-page [:div.ui.container (navbar request) content]))
   "text/html; charset=utf-8"))

(defn render-error
  "error should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [details]
  {:status (:status details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (error-page details)})
