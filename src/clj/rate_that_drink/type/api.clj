(ns rate-that-drink.type.api
  (:require [rate-that-drink.type.domain :as d]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]))

;; Errors

(s/def :bad-request/error #{"Bad Request"})
(s/def ::bad-request-response
  (s/keys :req-un [:bad-request/error]))

(def bad-request-response-body (g/generate (s/gen ::bad-request-response)))

(s/def :not-found/error #{"Not Found"})
(s/def ::not-found-response
  (s/keys :req-un [:not-found/error]))

(def not-found-response-body (g/generate (s/gen ::not-found-response)))

(s/def :unauthorized/error #{"Unauthorized"})
(s/def ::unauthorized-response
  (s/keys :req-un [:unauthorized/error]))

(def unauthorized-response-body (g/generate (s/gen ::unauthorized-response)))

(s/def :forbidden/error #{"Forbidden"})
(s/def ::forbidden-response
  (s/keys :req-un [:forbidden/error]))

(def forbidden-response-body (g/generate (s/gen ::forbidden-response)))

;; APIs

(s/def ::login-body-params
  (s/keys :req-un [::d/email
                   ::d/password]))

(s/def ::profile-response
  (s/keys :req-un [::d/id
                   ::d/email
                   ::d/first_name
                   ::d/last_name]))

(s/def ::post-profile-body-params
  (s/keys :req-un [::d/email
                   ::d/password
                   ::d/password_confirm
                   ::d/first_name
                   ::d/last_name]))

(s/def ::put-profile-path-params
  (s/keys :req-un [::d/id]))

(s/def ::put-profile-body-params
  (s/keys :opt-un [::d/email
                   ::d/first_name
                   ::d/last_name]))

(s/def ::id-only-path-params
  (s/keys :req-un [::d/id]))

(s/def ::drinks-response
  (s/coll-of ::d/drink))

(s/def ::drink-response ::d/drink)

(s/def ::drink-body-params
  (s/keys :req-un [::d/name
                   ::d/maker
                   ::d/type
                   ::d/style
                   ::d/appearance
                   ::d/smell
                   ::d/taste]
          :opt-un [::d/appearance_notes
                   ::d/smell_notes
                   ::d/taste_notes
                   ::d/comments]))
