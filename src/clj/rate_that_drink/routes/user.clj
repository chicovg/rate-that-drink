(ns rate-that-drink.routes.user
  (:require
   [ring.util.response :refer [redirect]]
   [rate-that-drink.db.core :as db]
   [rate-that-drink.layout :as layout]
   [rate-that-drink.middleware :as middleware]
   [rate-that-drink.schema :as sc]
   [rate-that-drink.routes.pages :as pages])
  (:use [ring.util.anti-forgery]))

(defn get-user-beers-page [request]
  (let [user_id (get-in request [:session :identity])
        beers (db/get-beers {:user_id user_id})]
    (layout/render request (pages/user-beers-page {:beers beers}))))

(defn- set-key
  [m key value-fn]
  (assoc m key (value-fn m)))

(defn get-user-drinks-page [request]
  (let [user_id (get-in request [:session :identity])
        page-params     (-> (:params request)
                            (assoc :user_id user_id)
                            (set-key :drinks db/query-drinks))]
    (layout/render request (pages/user-drinks-page page-params))))

(defn get-user-beer-form-page [request]
  (layout/render request (pages/user-beer-form-page {})))

(defn get-user-drink-form-page [request]
  (layout/render request (pages/user-drink-form-page {})))

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
      (if (db/create-beer! beer)
        (redirect "/user/beers")
        (layout/render request (pages/user-beer-form-page {:errors ["There was a problem saving, try again later."]})))
      (layout/render request (pages/user-beer-form-page {:errors schema-errors})))))

(def default-drink {:appearance "5"
                   :smell "5"
                   :taste "5"})

(defn create-new-drink [request]
  (let [drink (-> (merge default-drink (:params request))
                  (sc/convert-drink)
                  (assoc :user_id (get-in request [:session :identity])))
        schema-errors (sc/get-schema-errors drink sc/drink-schema)]
    (if (empty? schema-errors)
      (if (db/create-drink! drink)
        (redirect "/user/drinks")
        (layout/render request (pages/user-drink-form-page {:errors ["There was a problem saving, try again later."]})))
      (layout/render request (pages/user-drink-form-page {:errors schema-errors})))))

(defn get-user-edit-beer-page [request]
  (let [beer (db/get-beer (-> (:path-params request)
                              (update :id sc/parse-int)))]
    (layout/render request (pages/user-beer-form-page {:beer beer}))))

(defn get-user-edit-drink-page [request]
  (let [drink (db/get-drink (-> (:path-params request)
                                (update :id sc/parse-int)))]
    (layout/render request (pages/user-drink-form-page {:drink drink}))))

(defn update-beer [request]
  (let [beer (-> (:params request)
                 (sc/convert-beer)
                 (assoc :id (-> (get-in request [:path-params :id])
                                (sc/parse-int))))
        schema-errors (sc/get-schema-errors beer sc/beer-schema)]
    (if (empty? schema-errors)
      (if (db/update-beer! beer)
        (redirect "/user/beers")
        (layout/render request (pages/user-beer-form-page {:beer beer :errors ["There was a problem updating, try again later."]})))
      (layout/render request (pages/user-beer-form-page {:beer beer :errors schema-errors})))))

(defn update-drink [request]
  (let [drink (-> (:params request)
                 (sc/convert-drink)
                 (assoc :id (-> (get-in request [:path-params :id])
                                (sc/parse-int))))
        schema-errors (sc/get-schema-errors drink sc/drink-schema)]
    (if (empty? schema-errors)
      (if (db/update-drink! drink)
        (redirect "/user/drinks")
        (layout/render request (pages/user-drink-form-page {:drink drink :errors ["There was a problem updating, try again later."]})))
      (layout/render request (pages/user-drink-form-page {:drink drink :errors schema-errors})))))

(defn get-user-delete-beer-page [request]
  (layout/render request (pages/delete-beer-page (-> (:path-params request)
                                                     (update :id sc/parse-int)))))

(defn get-user-delete-drink-page [request]
  (layout/render request (pages/delete-beer-page (-> (:path-params request)
                                                     (update :id sc/parse-int)))))

(defn delete-beer [request]
  (let [id (-> (get-in request [:path-params :id])
               (sc/parse-int))]
    (if (db/delete-beer! {:id id})
      (redirect "/user/beers")
      (layout/render request (pages/delete-beer-page {:errors ["There was a problem deleting, try again later."]})))))

(defn delete-drink [request]
  (let [id (-> (get-in request [:path-params :id])
               (sc/parse-int))]
    (if (db/delete-drink! {:id id})
      (redirect "/user/drinks")
      (layout/render request (pages/delete-drink-page {:errors ["There was a problem deleting, try again later."]})))))

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
   ;; TODO remove
   ["/beers"            {:get get-user-beers-page}]
   ["/beers/delete/:id" {:get get-user-delete-beer-page
                         :post delete-beer}]
   ["/beers/edit/:id"   {:get get-user-edit-beer-page
                         :post update-beer}]
   ["/beers/new"        {:get get-user-beer-form-page
                         :post create-new-beer}]
   ;; end to be removed
   ["/drinks"           {:get get-user-drinks-page}]
   ["/drinks/delete/:id" {:get get-user-delete-beer-page
                         :post delete-drink}]
   ["/drinks/edit/:id"  {:get get-user-edit-drink-page
                         :post update-drink}]
   ["/drinks/new"       {:get get-user-drink-form-page
                         :post create-new-drink}]
   ["/profile"          {:get get-user-profile-page}]
   ["/logout"           {:get logout-user}]])
