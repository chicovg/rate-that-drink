(ns rate-that-drink.routes.services
  (:require
   [buddy.hashers :as h]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [rate-that-drink.db.core :as db]
   [rate-that-drink.middleware.exception :as exception]
   [rate-that-drink.middleware.formats :as formats]
   [rate-that-drink.type.api :as t]
   [ring.util.http-response :refer [bad-request
                                    conflict
                                    created
                                    forbidden
                                    no-content
                                    not-found
                                    ok
                                    unauthorized]]
   [clojure.string :as str]))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "rate-that-drink-api"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"
             :config {:validator-url nil}})}]]

   ["/login"
    {:tags ["auth"]
     :post {:summary "Login with email and password"
            :parameters {:body ::t/login-body-params}
            :responses {200 {:body ::t/profile-response}
                        401 {:body ::t/unauthorized-response}}
            :handler (fn [{{{:keys [password] :as user} :body} :parameters
                           session                             :session}]
                       (if-let [user (db/get-user-by-email user)]
                         (if (h/check password (:pass user))
                           (-> (db/get-user-by-email user)
                               (select-keys [:id :email :first_name :last_name])
                               ok
                               (assoc :session (assoc session :identity (:id user)
                                                      :first_name (:first_name user))))
                           (unauthorized t/unauthorized-response-body))
                         (unauthorized t/unauthorized-response-body)))}}]

   ["/logout"
    {:tags ["auth"]
     :put {:summary "Logs out the current user"
           :handler (fn [{session :session}]
                      (-> (no-content)
                          (assoc :session (dissoc session :identity))))}}]

   ["/profile"
    {:tags ["profile"]}
    [""
     {:get {:summary "Get the current authenticated profile, returns nothing if not authenticated"
            :responses {200 {:body ::t/profile-response}
                        204 {:body nil}}
            :handler (fn [{{identity :identity} :session}]
                       (if-let [user (db/get-user {:id identity})]
                         (ok user)
                         (no-content)))}
      :post {:summary "Create a new user profile"
             :parameters {:body ::t/post-profile-body-params}
             :responses {201 {:body ::t/profile-response}
                         400 {:body ::t/bad-request-response}}
             :handler (fn [{{{:keys [password password_confirm] :as user} :body} :parameters
                            session      :session}]
                        (let [user-by-email (db/get-user-by-email user)]
                          (cond
                            (not (nil? user-by-email))
                            (conflict {:error "A user with that email already exists"})

                            (not= password password_confirm)
                            (bad-request {:error "The provided passwords to not match"})

                            :else
                            (let [created-user (-> user
                                                   (assoc :pass (h/encrypt password))
                                                   db/create-user!
                                                   first)
                                  updated-session (assoc session :identity (:id created-user)
                                                                 :first_name (:first_name created-user))]
                              (-> (created (str "/api/profile") created-user)
                                  (assoc :session updated-session))))))}}]

    ["/{id}"
     {:put {:summary "Update an existing user profile"
            :parameters {:body ::t/put-profile-body-params
                         :path ::t/put-profile-path-params}
            :responses {200 {:body ::t/profile-response}
                        401 {:body ::t/unauthorized-response}
                        403 {:body ::t/forbidden-response}}
            :handler (fn [{{user     :body
                            {id :id} :path} :parameters
                           {identity :identity} :session}]
                       (let [user-by-id (db/get-user {:id id})]
                         (cond
                           (nil? identity)
                           (unauthorized t/unauthorized-response-body)

                           (not= (long id) (long identity))
                           (forbidden t/forbidden-response-body)

                           (nil? user-by-id)
                           (not-found t/not-found-response-body)

                           :else
                           (let [user-update (assoc user :id id)
                                 _ (db/update-user! user-update)]
                             (ok user-update)))))}
      :delete {:summary "Delete an existing user profile"
               :parameters {:path ::t/id-only-path-params}
               :responses {204 {:body nil}
                           401 {:body ::t/unauthorized-response}
                           403 {:body ::t/forbidden-response}}
               :handler (fn [{{{id :id} :path} :parameters
                              session          :session}]
                          (let [user-by-id (db/get-user {:id id})]
                            (cond
                              (nil? (:identity session))
                              (unauthorized t/unauthorized-response-body)

                              (not= (long id) (long {:identity session}))
                              (forbidden t/forbidden-response-body)

                              (nil? user-by-id)
                              (not-found t/not-found-response-body)

                              :else
                              (do
                                (db/delete-user! {:id id})
                                (-> (no-content)
                                    (assoc :session (dissoc session :identity)))))))}}]]

   ["/drinks"
    {:tags ["drinks"]}

    [""
     {:get {:summary "Gets all the drinks for the current user"
            :handler (fn [{{identity :identity} :session}]
                       (if identity
                         (ok (db/query-drinks {:user_id identity}))
                         (unauthorized t/unauthorized-response-body)))}
      :post {:summary "Creates a new drink for the current user"
             :parameters {:body ::t/drink-body-params}
             :responses {201 {:body ::t/drink-response}
                         401 {:body ::t/unauthorized-response}}
             :handler (fn [{{body     :body}     :parameters
                            {identity :identity} :session}]
                        (if identity
                          (as-> body $
                            (assoc $ :user_id identity)
                            (merge {:appearance_notes nil
                                    :smell_notes      nil
                                    :taste_notes      nil
                                    :comments         nil}
                                   $)
                            (db/create-drink! $)
                            (first $)
                            ((fn [{:keys [id] :as drink}]
                               (created (str "/api/drinks/" id) drink))
                             $))
                          (unauthorized t/unauthorized-response-body)))}}]

    ["/{id}"
     {:put {:summary "Updates the drink with given id and values"
            :parameters {:body ::t/drink-body-params
                         :path ::t/id-only-path-params}
            :responses {200 {:body ::t/drink-response}
                        401 {:body ::t/unauthorized-response}
                        403 {:body ::t/forbidden-response}
                        404 {:body ::t/not-found-response}}
            :handler (fn [{{body     :body
                            {id :id} :path}     :parameters
                           {identity :identity} :session}]
                       (let [drink-by-id (db/get-drink {:id id})]
                         (cond
                           (nil? identity)
                           (unauthorized t/unauthorized-response-body)

                           (nil? drink-by-id)
                           (not-found t/not-found-response-body)

                           (not= identity (:user_id drink-by-id))
                           (forbidden t/forbidden-response-body)

                           :else
                           (as-> body $
                             (merge {:appearance_notes nil
                                     :smell_notes      nil
                                     :taste_notes      nil
                                     :comments         nil}
                                    $)
                             (assoc $ :id id
                                    :user_id identity)
                             (db/update-drink! $)
                             (first $)
                             (ok $)))))}
      :delete {:summary "Deletes the drink with the given id"
               :parameters {:path ::t/id-only-path-params}
               :responses {204 {:body nil}
                           401 {:body ::t/unauthorized-response}
                           403 {:body ::t/forbidden-response}
                           404 {:body ::t/not-found-response}}
               :handler (fn [{{{id :id} :path}     :parameters
                              {identity :identity} :session}]
                          (let [drink-by-id (db/get-drink {:id id})]
                            (cond
                              (nil? identity)
                              (unauthorized t/unauthorized-response-body)

                              (nil? drink-by-id)
                              (not-found t/not-found-response-body)

                              (not= identity (:user_id drink-by-id))
                              (forbidden t/forbidden-response-body)

                              :else
                              (do
                                (db/delete-drink! {:id id})
                                (no-content)))))}}]]])
