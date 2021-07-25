(ns rate-that-drink.subs
  (:require [re-frame.core :as rf]
            [rate-that-drink.common :as common]
            [rate-that-drink.db :as db]))

(rf/reg-sub
 ::loading?
 (fn [db]
   (-> db ::db/loading? not-empty boolean)))

(rf/reg-sub
 ::profile
 (fn [db]
   (::db/profile db)))

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
 ::drinks-sort
 (fn [db] (::db/drinks-sort db)))

(rf/reg-sub
 ::error
 (fn [db [_ type]]
   (get-in db [::db/error type])))

(rf/reg-sub
 ::selected-drink-id
 (fn [db _]
   (::db/selected-drink-id db)))

(rf/reg-sub
 ::filtered-drinks
 :<- [::drinks]
 :<- [::drinks-filter]
 (fn [[drinks drinks-filter]]
   (->> drinks
        (common/filter-drinks drinks-filter)
        (map #(assoc % :total (common/calculate-ratings-total %))))))

(rf/reg-sub
 ::filtered-sorted-drinks
 :<- [::filtered-drinks]
 :<- [::drinks-sort]
 (fn [[drinks {:keys [direction field]}]]
   (sort (fn [drink-a drink-b]
           (if (= direction :ascending)
             (< (get drink-a field) (get drink-b field))
             (> (get drink-a field) (get drink-b field))))
         drinks)))

(rf/reg-sub
 ::drinks-page-count
 :<- [::filtered-drinks]
 :<- [::drinks-page-size]
 (fn [[drinks drinks-page-size]]
   (common/drinks-page-count drinks-page-size drinks)))

(rf/reg-sub
 ::paginated-drinks
 :<- [::filtered-sorted-drinks]
 :<- [::drinks-page]
 :<- [::drinks-page-size]
 (fn [[filtered-drinks drinks-page drinks-page-size]]
   (if (not-empty filtered-drinks)
     (as-> filtered-drinks $
       (partition-all drinks-page-size $)
       (nth $ (dec drinks-page)))
     filtered-drinks)))

(rf/reg-sub
 ::drinks-pages
 :<- [::drinks-page-count]
 (fn [drinks-page-count]
   (for [n (range 0 drinks-page-count)]
     (inc n))))

(rf/reg-sub
 ::selected-drink
 :<- [::drinks]
 :<- [::selected-drink-id]
 (fn [[drinks selected-drink-id]]
   (->> drinks
        (filter #(= selected-drink-id (:id %)))
        first)))
