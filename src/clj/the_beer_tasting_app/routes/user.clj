(ns the-beer-tasting-app.routes.user
  (:require
   [ring.util.response :refer [redirect bad-request]]
   [struct.core :as s]
   [the-beer-tasting-app.db.core :refer [*db*] :as db]
   [the-beer-tasting-app.layout :as layout]
   [the-beer-tasting-app.middleware :as middleware]
   [the-beer-tasting-app.schema :as sc])
  (:use [ring.util.anti-forgery]))

(defn user-beers-page []
  [:div.ui.segment
   [:div.beers-header
    [:h1 "Your Beers"]
    [:a {:href "/user/beers/new"} "Add New"]]])

(defn get-user-beers-page [request]
  (layout/render request (user-beers-page)))

(defn user-beer-form-page []
  [:div.ui.segment
   [:h1 "Add a new beer"]
   [:form.ui.form {:method "post"}
    [:div.field
     [:label "Name"]
     [:input {:type "text" :name "name" :placeholder "Beer Name" :required true}]]
    [:div.field
     [:label "Brewery"]
     [:input {:type "text" :name "brewery" :placeholder "Brewery" :required true}]]
    [:div.field
     [:label "Style"]
     [:input {:type "text" :name "style" :placeholder "Style" :required true}]]
    [:div.two.fields
     [:div.field
      [:label "Appearance"]
      [:input.rating {:type "number" :min 0 :max 10 :placeholder "Appearance"}]]
     [:div.field
      [:label "Smell"]
      [:input.rating {:type "number" :min 0 :max 20 :placeholder "Smell"}]]]
    [:div.two.fields
     [:div.field
      [:label "Taste"]
      [:input.rating {:type "number" :min 0 :max 30 :placeholder "Taste"}]]
     [:div.field.rating
      [:label "Aftertaste"]
      [:input.rating {:type "number" :min 0 :max 20 :placeholder "Aftertaste"}]]]
    [:div.two.fields
     [:div.field.rating
      [:label "Drinkability"]
      [:input.rating {:type "number" :min 0 :max 30 :placeholder "Drinkability"}]]
     [:div.field
      [:label "Total"]
      [:p.total 0]]]
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/user/beers"} "Cancel"]]])

(defn get-user-beer-form-page [request]
  (layout/render request (user-beer-form-page)))

(defn create-new-beer [request]
  (let [beer (-> (:params request)
                 (assoc :user_id (get-in request [:session :identity])))]
    (let [schema-errors (sc/get-schema-errors beer sc/beer-schema)]
      (if (empty? schema-errors)
        (redirect "/user/beers")
        (layout/render request (user-beer-form-page {:errors schema-errors}))))))

(defn user-profile-page []
  [:div.ui.segment
   [:h1 "User profile will go here"]])

(defn get-user-profile-page [request]
  (layout/render request (user-profile-page)))

(defn logout-user [request]
  (let [updated-session (-> request
                            (:session)
                            (dissoc :identity)
                            (dissoc :first_name))]
    (-> (redirect "/")
        (assoc :session updated-session))))

(defn user-routes []
  ["/user"
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-restricted
                 middleware/wrap-formats]}
   ["/beers" {:get get-user-beers-page}]
   ["/beers/new" {:get get-user-beer-form-page
                  :post create-new-beer}]
   ["/profile" {:get get-user-profile-page}]
   ["/logout" {:get logout-user}]])
