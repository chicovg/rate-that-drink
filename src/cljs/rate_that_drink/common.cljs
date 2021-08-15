(ns rate-that-drink.common
  (:require [clojure.string :as str]))

(defn- matches-filter?
  [drinks-filter drink]
  (some #(str/includes? % (str/lower-case drinks-filter))
        (->> [(:name drink) (:maker drink) (:type drink) (:style drink)]
             (map str/lower-case))))

(defn filter-drinks
  [drinks-filter drinks]
  (filter #(or (str/blank? drinks-filter)
               (matches-filter? drinks-filter %))
          drinks))

(defn drinks-page-count
  [drinks-page-size drinks]
  (as-> drinks $
    (count $)
    (/ $ drinks-page-size)
    (.ceil js/Math $)))

(defn calculate-ratings-total
  [{:keys [appearance
           smell
           taste]
    :or {appearance 0
         smell      0
         taste      0}}]
  (-> (+ appearance smell (* 3 taste))
      (/ 5)
      (.toFixed 1)))
