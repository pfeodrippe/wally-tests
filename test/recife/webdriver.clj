(ns recife.webdriver
  "Functions to help you webdrive a browser using Recife."
  (:require
   [clojure.test :refer [is testing report]]
   [medley.core :as m]
   [recife.core :as r]))

(comment

  (do
    (def result (r/get-result))
    (def max-number-of-traces 20)

    (def trace
      (-> result
          r/states-from-result
          (r/random-traces-from-states
           {:max-number-of-traces max-number-of-traces
            :max-number-of-states 10})
          first))

    (def initial-global-state {}))

  ())

(def ^:private original-report
  report)

(defn check-state
  [previous-state state states-mapping]
  (->> states-mapping
       (mapv (fn [[k {:keys [assertion save]}]]
               [k (if (contains? state k)
                    (let [*test-info (atom [])]
                      (with-redefs [report (fn [event]
                                             (swap! *test-info conj
                                                    {:event event
                                                     :testing-contexts
                                                     (vec (reverse clojure.test/*testing-contexts*))})
                                             (original-report event))]
                        (testing k
                          (def fgg assertion)
                          (let [result (assertion previous-state state)]
                            (if result
                              {:result result
                               :impl-state (save state)}
                              {:result result
                               :test-info @*test-info})))))
                    {:result true})]))
       (remove (comp :result second))
       (into {})))

(defn analyze-trace
  [trace {:keys [init procs-mapping states-mapping]}]
  (let [initial-global-state (dissoc (get-in trace [0 1]) ::r/procs)
        _ (init initial-global-state)]
    (loop [[[idx {:keys [recife/metadata] :as state}] & next-states] trace
           history []]
      (when idx
        (if metadata
          (let [previous-state (last (last history))
                {:keys [context]} metadata
                step (first context)
                step-fn (procs-mapping step)]
            (when-not step-fn
              (throw (ex-info "Handler not implemented for step" {:step step})))
            (step-fn context)
            (let [errors (check-state previous-state state states-mapping)]
              (if (seq errors)
                {:type :violation
                 :step step
                 :errors errors
                 :trace history}
                (recur next-states (conj history [idx state])))))

          ;; Initial state.
          (let [errors (check-state nil state states-mapping)]
            (if (seq errors)
              {:type :initial-state-violation
               :errors errors
               :trace (conj history [idx state])}
              (recur next-states (conj history [idx state])))))))))

#_{:clj-kondo/ignore [:unused-binding]}
(defn analyze
  "`max-number-of-states` drives how "
  [result {:keys [init procs states max-number-of-traces max-number-of-states]
           :or {max-number-of-traces 10
                max-number-of-states 10}
           :as params}]
  (let [params (update params
                       :states-mapping
                       ;; Adapt states mapping.
                       (fn [states-mapping]
                         (->> states-mapping
                              (m/map-vals (fn [{:keys [assertion save]
                                                :or {assertion (constantly true)
                                                     save identity}
                                                :as v}]
                                            (if (fn? v)
                                              {:assertion v
                                               :save identity}
                                              {:assertion assertion
                                               :save identity}))))))]
    (loop [[trace & other-traces] (-> result
                                      r/states-from-result
                                      (r/random-traces-from-states
                                       {:max-number-of-traces max-number-of-traces
                                        :max-number-of-states max-number-of-states}))
           trace-counter 0]
      (if trace
        (let [result (analyze-trace trace params)]
          (if (:errors result)
            result
            (recur other-traces (inc trace-counter))))
        {:ok :ok}))))
