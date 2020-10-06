(ns the-beer-tasting-app.handler-test
  (:require
   [clojure.test :refer :all]
   [hickory.core :as h]
   [hickory.select :as s]
   [ring.mock.request :refer :all]
   [the-beer-tasting-app.handler :refer :all]
   [the-beer-tasting-app.middleware.formats :as formats]
   [muuntaja.core :as m]
   [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(defn parse-response-body [body]
  (-> body
      (h/parse)
      (h/as-hickory)))

(defn select-page-header [parsed-body]
  (->> parsed-body
       (s/select (s/descendant
                  (s/tag :body)
                  (s/and (s/tag :div)
                         (s/class "segment"))
                  (s/tag :h1)))
       first
       :content
       first))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'the-beer-tasting-app.config/env
                 #'the-beer-tasting-app.handler/app-routes)
    (f)))

(deftest test-home-routes
  (testing "landing page"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))
      (is (= "Welcome to the 'Rate that beer' app" (->> (:body response)
                                                        parse-response-body
                                                        select-page-header)))))
  (testing "profile page"
    (let [response ((app) (request :get "/profile"))]
      (is (= 200 (:status response)))
      (is (= "Create an account" (->> (:body response)
                                      parse-response-body
                                      select-page-header)))))
  (testing "login page"
    (let [response ((app) (request :get "/login"))]
      (is (= 200 (:status response)))
      (is (= "Enter your email and password" (->> (:body response)
                                                  parse-response-body
                                                  select-page-header)))))
  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))

(deftest test-user-routes
  (testing "beers page redirects when not logged in"
    (let [response ((app) (request :get "/user/beers"))]
      (is (= 302 (:status response))))))
