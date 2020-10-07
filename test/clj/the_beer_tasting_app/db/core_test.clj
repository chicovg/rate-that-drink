(ns the-beer-tasting-app.db.core-test
  (:require
   [the-beer-tasting-app.db.core :refer [*db*] :as db]
   [java-time.pre-java8]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]
   [the-beer-tasting-app.config :refer [env]]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'the-beer-tasting-app.config/env
     #'the-beer-tasting-app.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-users
  (jdbc/with-transaction [conn *db* {:rollback-only true}]
    (let [{id :id} (first (db/create-user! conn {:first_name "Bob"
                                                 :last_name "Slidell"
                                                 :email "bob.slidell@initech.com"
                                                 :pass "pass"}))]
      (is (= {:id id
              :first_name "Bob"
              :last_name "Slidell"
              :email "bob.slidell@initech.com"}
             (db/get-user conn {:id id}))))))

(def beer {:name "Test name"
           :brewery "Test brewery"
           :style "Test style"
           :appearance 30
           :smell 10
           :taste 18
           :aftertaste 22
           :drinkability 29})

(deftest test-beers
  (jdbc/with-transaction [conn *db* {:rollback-only true}]
    (let [{user_id :id} (-> (db/create-user! conn {:first_name "Johnny"
                                                   :last_name "Karate"
                                                   :email "jkicks@hitmail.com"
                                                   :pass "chop"})
                            first)
          {id :id} (-> (db/create-beer! conn (assoc beer :user_id user_id))
                       (first))]
      (is (not (nil? user_id)))
      (is (not (nil? id)))
      (is (=  (-> beer
                  (assoc :id id)
                  (assoc :user_id user_id))
              (-> (db/get-beer conn {:id id})
                  (dissoc :created_at)
                  (dissoc :updated_at))))
      (is (some (fn [{beer-id :id}] (= id beer-id))
                (db/get-beers conn {:user_id user_id})))
      (is (= 1 (->> (assoc (db/get-beer conn {:id id}) :name "Updated")
                    (db/update-beer! conn))))
      (is (= "Updated" (:name (db/get-beer conn {:id id}))))
      (is (= 1 (db/delete-beer! conn {:id id})))
      (is (nil? (db/get-beer conn {:id id})))
      (is (= 1 (db/delete-user! conn {:id user_id}))))))
