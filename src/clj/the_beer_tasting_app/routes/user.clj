(ns the-beer-tasting-app.routes.user
  (:require
   [ring.util.response :refer [redirect bad-request]]
   [the-beer-tasting-app.db.core :refer [*db*] :as db]
   [the-beer-tasting-app.layout :as layout]
   [the-beer-tasting-app.middleware :as middleware]
   [the-beer-tasting-app.schema :as sc]
   [the-beer-tasting-app.routes.pages :as pages])
  (:use [ring.util.anti-forgery]))

(defn get-user-beers-page [request]
  (let [user_id (get-in request [:session :identity])
        beers (db/get-beers *db* {:user_id user_id})]
    (layout/render request (pages/user-beers-page {:beers beers}))))

(defn get-user-beer-form-page [request]
  (layout/render request (pages/user-beer-form-page {})))

(def default-beer {:appearance "10"
                   :smell "20"
                   :taste "30"
                   :aftertaste "20"
                   :drinkability "30"
                   :comments ""})

(defn create-new-beer [request]
  (let [beer (-> (merge default-beer (:params request))
                 (sc/convert-beer)
                 (assoc :user_id (get-in request [:session :identity])))
        schema-errors (sc/get-schema-errors beer sc/beer-schema)]
    (if (empty? schema-errors)
      (if (db/create-beer! *db* beer)
        (redirect "/user/beers")
        (layout/render request (pages/user-beer-form-page {:errors ["There was a problem saving, try again later."]})))
      (layout/render request (pages/user-beer-form-page {:errors schema-errors})))))

(defn get-user-edit-beer-page [request]
  (let [beer (db/get-beer *db* (-> (:path-params request)
                                   (update :id sc/parse-int)))]
    (layout/render request (pages/user-beer-form-page {:beer beer}))))

(defn update-beer [request]
  (let [beer (-> (:params request)
                 (sc/convert-beer)
                 (assoc :id (-> (get-in request [:path-params :id])
                                (sc/parse-int))))
        schema-errors (sc/get-schema-errors beer sc/beer-schema)]
    (if (empty? schema-errors)
      (if (db/update-beer! *db* beer)
        (redirect "/user/beers")
        (layout/render request (pages/user-beer-form-page {:beer beer :errors ["There was a problem updating, try again later."]})))
      (layout/render request (pages/user-beer-form-page {:beer beer :errors schema-errors})))))

(defn get-user-delete-beer-page [request]
  (layout/render request (pages/delete-beer-page (-> (:path-params request)
                                                     (update :id sc/parse-int)))))

(defn delete-beer [request]
  (let [id (-> (get-in request [:path-params :id])
               (sc/parse-int))]
    (if (db/delete-beer! *db* {:id id})
      (redirect "/user/beers")
      (layout/render request (pages/delete-beer-page {:errors ["There was a problem deleting, try again later."]})))))

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
   ["/beers/delete/:id" {:get get-user-delete-beer-page
                         :post delete-beer}]
   ["/beers/edit/:id" {:get get-user-edit-beer-page
                       :post update-beer}]
   ["/profile" {:get get-user-profile-page}]
   ["/logout" {:get logout-user}]])
