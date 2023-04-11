(ns com.pfeodrippe.wally.audio-player
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :refer [is]]
   [recife.core :as r]
   [recife.helpers :as rh]
   [recife.webdriver :as rw]
   [wally.main :as w]))

(def global
  {::playing? false
   ::time 0})

(r/defproc play
  (fn [{::keys [playing?] :as db}]
    (when (not playing?)
      (assoc db ::playing? true))))

(r/defproc pause
  (fn [{::keys [playing?] :as db}]
    (when playing?
      (assoc db ::playing? false))))

(r/defproc tick
  (fn [{::keys [playing?] :as db}]
    (when playing?
      (update db ::time inc))))

;; We add a constraint so ◊code{time} does not evolve forever.
(rh/defconstraint time-constraint
  [{::keys [time]}]
  (< time 10))

(comment

  @(r/run-model global #{play pause tick time-constraint} {:trace-example true})
  (r/random-traces-from-result (r/get-result) {:max-number-of-traces 1
                                               :max-number-of-states 10})

  (let [init (fn  [_]
               (w/navigate (str (io/resource "audioplayer.html"))))

        procs-mapping {::play
                       (fn [_]
                         (w/click :play-pause))

                       ::pause
                       (fn [_]
                         (w/click :play-pause))

                       ::tick
                       (fn [_]
                         (Thread/sleep 1000))}

        states-mapping {::playing?
                        (fn [_ {::keys [playing?]}]
                          (if playing?
                            (is (= "Pause" (w/text-content :play-pause)))
                            (is (= "Play" (w/text-content :play-pause)))))

                        ::time
                        {:check
                         (fn [previous-db db]
                           (if-let [previous-time (-> previous-db :-impl ::time)]
                             (is (<= previous-time (-> db :-impl ::time)))
                             true))

                         :snapshot
                         (fn [_]
                           (let [[minutes seconds] (->> (-> (w/text-content :time-display)
                                                            (str/split #":"))
                                                        (mapv #(Integer/parseInt %)))]
                             (+ (* 60 minutes) seconds)))}}]

    (rw/drive (r/get-result)
              {:init init
               :procs-mapping procs-mapping
               :states-mapping states-mapping
               :max-number-of-traces 1
               :max-number-of-states 10}))

  ())

;; TODO:
;; - [x] Create spec as in https://docs.quickstrom.io/en/latest/tutorials/first.html
;;   - [x] Play
;;   - [x] Pause
;;   - [x] Tick
;;     - [x] We may need something to tell us about old state (in the implemenation)
;;           so we can use invariants
