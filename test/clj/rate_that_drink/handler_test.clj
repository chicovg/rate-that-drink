(ns rate-that-drink.handler-test
  (:require
   [clojure.test :refer :all]
   [hickory.core :as h]
   [hickory.select :as s]
   [ring.mock.request :refer :all]
   [rate-that-drink.handler :refer :all]
   [rate-that-drink.middleware.formats :as formats]
   [muuntaja.core :as m]
   [mount.core :as mount])
  (:use [ring.util.anti-forgery]))

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

(defn select-anti-forgery-token [parsed-body]
  (->> parsed-body
       (s/select (s/descendant
                  (s/tag :body)
                  (s/tag :form)
                  (s/and (s/tag :input)
                         (s/id "__anti-forgery-token"))))
       first
       :attrs
       :value))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'rate-that-drink.config/env
                 #'rate-that-drink.handler/app-routes)
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
