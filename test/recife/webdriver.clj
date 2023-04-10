(ns recife.webdriver
  "Functions to help you webdrive a browser using Recife."
  (:require
   [recife.core :as r]
   [clojure.test :refer [is testing report]]))

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
  [state states-mapping]
  (->> states-mapping
       (mapv (fn [[k handler]]
               [k (if (contains? state k)
                    (let [*test-info (atom [])]
                      (with-redefs [report (fn [event]
                                             (swap! *test-info conj
                                                    {:event event
                                                     :testing-contexts (vec clojure.test/*testing-contexts*)})
                                             (original-report event))]
                        (testing k
                          (let [result (handler (get state k))]
                            (if result
                              {:result result}
                              {:result result
                               :test-info @*test-info})))))
                    {:result true})]))
       (remove (comp :result second))
       (into {})))

(defn analyze-trace
  [trace {:keys [init procs-mapping states-mapping]}]
  (let [initial-global-state (dissoc (get-in trace [0 1]) ::r/procs)
        _ (init initial-global-state)]
    (loop [[[idx {:keys [recife/metadata] :as state}] & next-states]
           (take 2 trace)

           history []]
      (when idx
        (if metadata
          (let [{:keys [context]} metadata
                step (first context)
                step-fn (procs-mapping step)]
            (when-not step-fn
              (throw (ex-info "Handler not implemented for step" {:step step})))
            (step-fn context)
            (let [errors (check-state state states-mapping)]
              (if (seq errors)
                {:type :violation
                 :step step
                 :errors errors
                 :trace history}
                (recur next-states (conj history)))))

          (let [errors (check-state state states-mapping)]
            (if (seq errors)
              {:type :initial-state-violation
               :errors errors
               :trace (conj history [idx state])}
              (recur next-states (conj history [idx state])))))))))

#_{:clj-kondo/ignore [:unused-binding]}
(defn analyze
  [result {:keys [init procs states max-number-of-traces]
           :or {max-number-of-traces 20}
           :as params}]
  (loop [[trace & other-traces] (-> result
                                    r/states-from-result
                                    (r/random-traces-from-states
                                     {:max-number-of-traces max-number-of-traces
                                      :max-number-of-states 10}))
         trace-counter 0]
    (if trace
      (let [result (analyze-trace trace params)]
        (recur other-traces (inc trace-counter)))
      {:ok :ok})))

(comment

  (do
    (require '[wally.main :as w])
    (def init
      (fn  [_]
        (w/navigate "file:///Users/paulo.feodrippe/dev/wally-tests/audioplayer.html")))

    (def procs-mapping {:com.pfeodrippe.wally.webdriver/play
                        (fn [_]
                          (w/click :play-pause))

                        :com.pfeodrippe.wally.webdriver/pause
                        (fn [_]
                          (w/click :play-pause))})

    (def states-mapping {:com.pfeodrippe.wally.webdriver/playing?
                         (fn [v]
                           (if v
                             (is (= "Pause" (w/text-content :play-pause)))
                             (is (= "Play" (w/text-content :play-pause)))))}))

  ())
