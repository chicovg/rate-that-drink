(ns rate-that-drink.subs
  (:require [re-frame.core :as rf]
            [rate-that-drink.common :as common]
            [rate-that-drink.db :as db]
            [clojure.string :as str]))

(rf/reg-sub
 ::loading?
 (fn [db]
   (-> db ::db/loading? not-empty boolean)))

(rf/reg-sub
 ::profile
 (fn [db]
   (::db/profile db)))

;; TODO total is already coming back from server, need to convert it
(defn calculate-total
  [{:keys [appearance
           smell
           taste]
    :as drink}]
  (assoc drink :total (-> (+ appearance smell taste)
                          (/ 5)
                          (.toFixed 1))))

(rf/reg-sub
 ::drinks
 (fn [db] (::db/drinks db)))

(rf/reg-sub
 ::drinks-filter
 (fn [db] (::db/drinks-filter db)))

(rf/reg-sub
 ::drinks-page
 (fn [db] (::db/drinks-page db)))

(rf/reg-sub
 ::drinks-page-size
 (fn [db] (::db/drinks-page-size db)))

(rf/reg-sub
 ::filtered-drinks
 :<- [::drinks]
 :<- [::drinks-filter]
 (fn [[drinks drinks-filter]]
   (->> drinks
        (common/filter-drinks drinks-filter)
        (map calculate-total))))

(rf/reg-sub
 ::drinks-page-count
 :<- [::filtered-drinks]
 :<- [::drinks-page-size]
 (fn [[drinks drinks-page-size]]
   (common/drinks-page-count drinks-page-size drinks)))

(rf/reg-sub
 ::paginated-drinks
 :<- [::filtered-drinks]
 :<- [::drinks-page]
 :<- [::drinks-page-size]
 (fn [[filtered-drinks drinks-page drinks-page-size]]
   (if (not-empty filtered-drinks)
     (as-> filtered-drinks $
       (partition-all drinks-page-size $)
       (nth $ drinks-page))
     filtered-drinks)))

(defn visible-range
  [page-count selected-page]
  (let [max (if (> selected-page (- page-count 3))
              (- page-count 1)
              (max 4 (+ selected-page 2)))
        min (- max 4)]
    [min max]))

(defn within-visible-range?
  [page-count selected-page page]
  (if (> page-count 5)
    (let [[min max] (visible-range page-count selected-page)]
      (and (>= page min)
           (<= page max)))
    true))

(rf/reg-sub
 ::drinks-pages
 :<- [::drinks-page]
 :<- [::drinks-page-count]
 (fn [[drinks-page drinks-page-count]]
   (for [n (range 0 drinks-page-count)
         :when (within-visible-range? drinks-page-count drinks-page n)]
     n)))
