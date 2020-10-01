(ns the-beer-tasting-app.schema
  (:require [struct.core :as st]))

(def user-schema [[:first_name st/string st/required]
                  [:last_name st/string st/required]
                  [:email st/email st/required]
                  [:pass st/string-like st/required]
                  [:confirm-pass st/string-like]])
