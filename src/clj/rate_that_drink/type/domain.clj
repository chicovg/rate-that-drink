(ns rate-that-drink.type.domain
  (:require [clojure.spec.alpha :as s]))

(s/def ::id number?)
(s/def ::first_name string?)
(s/def ::last_name string?)

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(defn email?
  [s]
  (re-matches email-regex s))

(s/def ::email (s/and string? email?))
(s/def ::password string?)
(s/def ::password_confirm string?)

(s/def ::user
  (s/keys :req-un [::first_name
                   ::last_name
                   ::email
                   ::password]))

(s/def ::string-10-chars (s/and string?
                                #(<= (count %) 10)))
(s/def ::string-100-chars (s/and string?
                                 #(<= (count %) 100)))

(s/def ::name ::string-100-chars)
(s/def ::maker ::string-100-chars)
(s/def ::type ::string-10-chars)
(s/def ::style ::string-100-chars)
(s/def ::rating #{1 2 3 4 5})
(s/def ::appearance ::rating)
(s/def ::appearance_notes (s/nilable string?))
(s/def ::smell ::rating)
(s/def ::smell_notes (s/nilable string?))
(s/def ::taste ::rating)
(s/def ::taste_notes (s/nilable string?))
(s/def ::comments (s/nilable string?))

(s/def ::drink
  (s/keys :req-un [::id
                   ::name
                   ::maker
                   ::type
                   ::appearance
                   ::smell
                   ::taste]
          :opt-un [::appearance_notes
                   ::smell_notes
                   ::taste_notes
                   ::comments]))
