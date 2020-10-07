(ns the-beer-tasting-app.routes.user
  (:require
   [ring.util.response :refer [redirect bad-request]]
   [struct.core :as s]
   [the-beer-tasting-app.db.core :refer [*db*] :as db]
   [the-beer-tasting-app.layout :as layout]
   [the-beer-tasting-app.middleware :as middleware]
   [the-beer-tasting-app.schema :as sc])
  (:use [ring.util.anti-forgery]))

(defn user-beers-page [user_id]
  (let [beers (db/get-beers *db* {:user_id user_id})]
    [:div.ui.segment
     [:div.beers-header
      [:h1 "Your Beers"]
      [:a {:href "/user/beers/new"} "Add New"]]
     [:table#beers.ui.selectable.celled.table
      [:thead
       [:tr
        [:th "Name"]
        [:th "Brewery"]
        [:th "Style"]
        [:th.one.wide "Rating"]]]
      [:tbody
       (for [{:keys [name
                     brewery
                     style] :as beer} beers]
         [:tr {:data-beer-id (:id beer)}
          [:td {:data-label "Name"} name]
          [:td {:data-label "Brewery"} brewery]
          [:td {:data-label "Style"} style]
          [:td {:data-label "Rating"} (sc/beer-total beer)]])]]]))

(defn get-user-beers-page [request]
  (layout/render request (user-beers-page (get-in request [:session :identity]))))

(defn user-beer-form-page [{beer :beer errors :errors}]
  [:div.ui.segment
   [:h1 (if beer "Update your beer" "Add a new beer")]
   (when (not-empty errors)
     [:div.ui.error.message
      (for [error errors]
        [:p error])])
   [:form.ui.form {:method (if beer "put" "post")}
    [:div.field
     [:label "Name"]
     [:input {:type "text"
              :name "name"
              :placeholder "Beer Name"
              :required true
              :value (:name beer)}]]
    [:div.field
     [:label "Brewery"]
     [:input {:type "text"
              :name "brewery"
              :placeholder "Brewery"
              :required true
              :value (:brewery beer)}]]
    [:div.field
     [:label "Style"]
     [:input {:type "text"
              :name "style"
              :placeholder "Style"
              :required true
              :value (:style beer)}]]
    [:div.two.fields
     [:div.field
      [:label "Appearance"]
      [:input.rating {:name "appearance"
                      :type "number"
                      :min 0
                      :max 10
                      :placeholder "Appearance (0-10)"
                      :value (:appearance beer)}]]
     [:div.field
      [:label "Smell"]
      [:input.rating {:name "smell"
                      :type "number"
                      :min 0
                      :max 20
                      :placeholder "Smell (0-20)"
                      :value (:smell beer)}]]]
    [:div.two.fields
     [:div.field
      [:label "Taste"]
      [:input.rating {:name "taste"
                      :type "number"
                      :min 0
                      :max 30
                      :placeholder "Taste (0-30)"
                      :value (:taste beer)}]]
     [:div.field.rating
      [:label "Aftertaste"]
      [:input.rating {:name "aftertaste"
                      :type "number"
                      :min 0
                      :max 20
                      :placeholder "Aftertaste (0-20)"
                      :value (:aftertaste beer)}]]]
    [:div.two.fields
     [:div.field.rating
      [:label "Drinkability"]
      [:input.rating {:name "drinkability"
                      :type "number"
                      :min 0
                      :max 30
                      :placeholder "Drinkability (0-30)"
                      :value (:drinkability beer)}]]
     [:div.field
      [:label "Total"]
      [:p.total (sc/beer-total beer)]]]
    (anti-forgery-field)
    [:div.ui.horizontal.divider]
    [:button.ui.button.primary {:type "submit"} "Submit"]
    [:a.ui.button {:href "/user/beers"} "Cancel"]]])

(defn get-user-beer-form-page [request]
  (layout/render request (user-beer-form-page {})))

(defn get-user-edit-beer-page [request]
  (let [beer (db/get-beer *db* (-> (:path-params request)
                                   (update :id sc/parse-int)))]
    (layout/render request (user-beer-form-page {:beer beer}))))

(def default-beer {:appearance "10"
                   :smell "20"
                   :taste "30"
                   :aftertaste "20"
                   :drinkability "30"})

(defn create-new-beer [request]
  (let [beer (-> (merge default-beer (:params request))
                 (sc/convert-beer)
                 (assoc :user_id (get-in request [:session :identity])))
        schema-errors (sc/get-schema-errors beer sc/beer-schema)]
    (if (empty? schema-errors)
      (if (db/create-beer! *db* beer)
        (redirect "/user/beers")
        (layout/render request (user-beer-form-page {:errors ["There was a problem saving, try again later."]})))
      (layout/render request (user-beer-form-page {:errors schema-errors})))))

(defn update-beer [request]
  (let [beer (-> (:params request)
                 (sc/convert-beer)
                 (assoc :id (-> (get-in request [:path-params :id])
                                (sc/parse-int))))
        schema-errors (sc/get-schema-errors beer sc/beer-schema)]
    (if (empty? schema-errors)
      (if (db/update-beer! *db* beer)
        (redirect "/user/beers")
        (layout/render request (user-beer-form-page {:errors ["There was a problem updating, try again later."]}))))
      (layout/render request (user-beer-form-page {:errors schema-errors}))))

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
   ["/beers/edit/:id" {:get get-user-edit-beer-page
                       :put update-beer}]
   ["/profile" {:get get-user-profile-page}]
   ["/logout" {:get logout-user}]])
