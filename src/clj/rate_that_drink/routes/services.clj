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
   [ring.util.http-response :refer [bad-request
                                    conflict
                                    forbidden
                                    no-content
                                    not-found
                                    ok
                                    unauthorized]]
   [clojure.java.io :as io]))

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
    {:post {:summary "Login with email and password"
            :parameters {:body {:email string?
                                :password string?}}
            :responses {200 {:body {:email string?}}
                        401 {:body {:error string?}}}
            :handler (fn [{{{:keys [password] :as user} :body} :parameters
                           session                          :session}]
                       (if-let [user (db/get-user-by-email user)]
                         (if (h/check password (:pass user))
                           (-> (db/get-user-by-email user)
                               (select-keys [:id :email :first_name :last_name])
                               ok
                               (assoc :session (assoc session :identity (:id user)
                                                              :first_name (:first_name user))))
                           (unauthorized {:error "Unauthorized."}))
                         (unauthorized {:error "Unauthorized."})))}}]

   ["/profile"
    [""
     {:get {:summary "Get the current authenticated profile, returns nothing if not authenticated"
            :responses {200 {:body {:id int?
                                    :email string?
                                    :first_name string?
                                    :last_name string?}}
                        204 {:body nil}}
            :handler (fn [{{identity :identity} :session}]
                       (if-let [user (db/get-user {:id identity})]
                         (ok user)
                         (no-content)))}
      :post {:summary "Create a new user profile"
             :parameters {:body {:email string?
                                 :password string?
                                 :password_confirm string?
                                 :first_name string?
                                 :last_name string?}}
             :responses {200 {:body {:id int?
                                     :email string?
                                     :first_name string?
                                     :last_name string?}}}
             :handler (fn [{{{:keys [password password_confirm] :as user} :body} :parameters
                            session      :session}]
                        (let [user-by-email (db/get-user-by-email user)]
                          (cond
                            (not (nil? user-by-email))
                            (conflict {:error "A user with that email already exists"})

                            (not= password password_confirm)
                            (bad-request {:error "The provided passwords to not match"})

                            :else
                            (let [created-user-id (-> user
                                                      (assoc :pass (h/encrypt password))
                                                      db/create-user!
                                                      first
                                                      :id)
                                  response        (select-keys user [:email :first_name :last_name])]
                              (-> (ok (assoc response :id created-user-id))
                                  (assoc :session (assoc session :identity created-user-id
                                                         :first_name (:first_name user))))))))}}]

    ["/{id}"
     {:put {:summary "Update an existing user profile"
            :parameters {:body {:email string?
                                :first_name string?
                                :last_name string?}
                         :path {:id number?}}
            :responses {200 {:id string?
                             :email string?
                             :first_name string?
                             :last_name string?}
                        401 {:error string?}
                        403 {:error string?}}
            :handler (fn [{{user     :body
                            {id :id} :path} :parameters
                           {identity :identity} :session}]
                       (let [user-by-id (db/get-user {:id id})]
                         (cond
                           (nil? identity)
                           (unauthorized {:error "Unauthorized."})

                           (not= (long id) (long identity))
                           (forbidden {:error "Forbidden."})

                           (nil? user-by-id)
                           (not-found {:error (str "No user exisits with id=" id)})

                           :else
                           (let [user-update (assoc user :id id)
                                 _ (db/update-user! user-update)]
                             (ok user-update)))))}}]]])
