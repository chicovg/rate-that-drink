(ns rate-that-drink.db)

(def initial-db {::drinks           []
                 ::drinks-filter    ""
                 ::drinks-page      1
                 ::drinks-page-size 15
                 ::drinks-sort      {:field     :total
                                     :direction :descending}
                 ::error            {}
                 ::profile          nil
                 ::loading?         {}
                 ::paging           {}
                 ::selected-drink   nil})
