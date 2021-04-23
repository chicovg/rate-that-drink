(ns rate-that-drink.schema
  (:require [struct.core :as st]))

(def user-schema [[:first_name st/string st/required]
                  [:last_name st/string st/required]
                  [:email st/email st/required]
                  [:pass st/string-like st/required]
                  [:confirm-pass st/string-like]])

(def beer-schema [[:name st/string st/required]
                  [:brewery st/string st/required]
                  [:style st/string st/required]
                  [:appearance st/number]
                  [:smell st/number]
                  [:taste st/number]
                  [:aftertaste st/number]
                  [:drinkability st/number]])

(def drink-rating {:message "must be a number from 1 to 5"
                   :optional true
                   :validate (fn [n] (and (>= n 1)
                                          (<= n 5)))})

(def drink-schema [[:name st/string st/required]
                   [:maker st/string st/required]
                   [:type st/string st/required]
                   [:appearance st/number st/required drink-rating]
                   [:smell st/number st/required drink-rating]
                   [:taste st/number st/required drink-rating]])

(defn get-schema-errors [object schema]
  (let [schema-errors (first (st/validate object schema))]
    (if (nil? schema-errors)
      []
      (for [[field message] schema-errors]
        (str (name field) " " message)))))

(defn parse-int [int-string]
  (when int-string
    (Integer/parseInt int-string)))

(defn convert-beer [beer]
  (-> beer
      (update :appearance parse-int)
      (update :smell parse-int)
      (update :taste parse-int)
      (update :aftertaste parse-int)
      (update :drinkability parse-int)))

(defn beer-total [beer]
  (->> [:appearance :smell :taste :aftertaste :drinkability]
      (map #(% beer 0))
      (reduce +)))

(defn convert-drink [drink]
  (-> drink
      (update :appearance parse-int)
      (update :smell parse-int)
      (update :taste parse-int)))
